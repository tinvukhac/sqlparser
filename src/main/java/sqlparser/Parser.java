package sqlparser;

import java.util.List;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.test.CalciteAssert;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.util.Holder;
import org.apache.calcite.util.Util;

import com.google.common.base.Function;
import com.google.protobuf.util.JsonFormat;

import edu.ucr.cs.qpe.RelationProtos.Relation;
import edu.ucr.cs.qpe.RelationProtos.Relation.Builder;

public class Parser {

	public Parser() {
	}

	public Planner getPlanner() throws Exception {
		return Frameworks.getPlanner(config().build());
	}

	private Frameworks.ConfigBuilder config() throws Exception {
		final Holder<SchemaPlus> root = Holder.of(null);
		CalciteAssert.model(TPCDS_MODEL).doWithConnection(new Function<CalciteConnection, Object>() {
			public Object apply(CalciteConnection input) {
				root.set(input.getRootSchema().getSubSchema("TPCDS"));
				return null;
			}
		});
		return Frameworks.newConfigBuilder().parserConfig(SqlParser.Config.DEFAULT).defaultSchema(root.get())
				.traitDefs((List<RelTraitDef>) null).programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2));
	}

	private static String schema(String name, String scaleFactor) {
		return "     {\n" + "       type: 'custom',\n" + "       name: '" + name + "',\n"
				+ "       factory: 'org.apache.calcite.adapter.tpcds.TpcdsSchemaFactory',\n" + "       operand: {\n"
				+ "         columnPrefix: true,\n" + "         scale: " + scaleFactor + "\n" + "       }\n" + "     }";
	}

	private static final String TPCDS_MODEL = "{\n" + "  version: '1.0',\n" + "  defaultSchema: 'TPCDS',\n"
			+ "   schemas: [\n" + schema("TPCDS", "1.0") + ",\n" + schema("TPCDS_01", "0.01") + ",\n"
			+ schema("TPCDS_5", "5.0") + "\n" + "   ]\n" + "}";

	public String toString(RelNode rel, String format) {
		return Util.toLinux(RelOptUtil.dumpPlan("", rel,
				format.equals("json") ? SqlExplainFormat.JSON : SqlExplainFormat.TEXT, SqlExplainLevel.ALL_ATTRIBUTES));
	}

	private String toString(RelNode rel) {
		return Util.toLinux(RelOptUtil.dumpPlan("", rel, SqlExplainFormat.JSON, SqlExplainLevel.ALL_ATTRIBUTES));
	}

	private Relation getRelation(RelNode rel) {
		RelMetadataQuery mq = rel.getCluster().getMetadataQuery();
		double rowCount = mq.getRowCount(rel);
		RelOptCost optCost = mq.getCumulativeCost(rel);

		Builder builder = Relation.newBuilder();
		builder.setOp(Relation.OpCode.valueOf(rel.getRelTypeName()));
		builder.setRowCount(rowCount);
		builder.setCumulativeCost(Relation.CumulativeCost.newBuilder().setRows(optCost.getRows())
				.setCpu(optCost.getCpu()).setIo(optCost.getIo()).build());
		List<RelNode> rels = rel.getInputs();
		if (rels.size() > 0) {
			for (RelNode relNode : rels) {
				Relation relation = getRelation(relNode);
				builder.addRelations(relation);
			}
		}

		return builder.build();
	}

	public String toJsonString(String query, int bestIndex) throws Exception {

		// Parse the query to a tree of relational expressions
		Planner planner = this.getPlanner();
		SqlNode parse = planner.parse(query);
		SqlNode validate = planner.validate(parse);
		RelNode rel = planner.rel(validate).project();

		// Build the Protobuf message from the relational expression tree
		Relation relation = this.getRelation(rel);
		Builder builder = relation.toBuilder();
		builder.setResult(bestIndex);
		relation = builder.build();

		return JsonFormat.printer().print(relation);
	}
}
