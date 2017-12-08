package sqlparser;

import java.io.File;
import java.util.List;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
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
import org.apache.commons.io.FileUtils;

import com.google.common.base.Function;

import edu.ucr.cs.qpe.RelationProtos.Relation;
import edu.ucr.cs.qpe.RelationProtos.Relation.Builder;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String filename = args[0];
		String format = args[1];
		
		String query = FileUtils.readFileToString(new File(filename));
		query = query.trim();
		query = query.substring(0, query.length() - 1);
		Main main = new Main();
		Planner planner = main.getPlanner();
		SqlNode parse = planner.parse(query);
		SqlNode validate = planner.validate(parse);
	    RelNode rel = planner.rel(validate).project();
//	    System.out.println(main.toString(rel, format));
	    
	    Builder builder = Relation.newBuilder();
	    
	    builder.setWidth(10);
	    builder.setRows(100);
	    Relation relation1 = builder.build();
	    
	    builder.setWidth(15);
	    builder.setRows(200);
	    Relation relation2 = builder.build();
	    
	    builder.setResult(1);
	    builder.addRelations(relation1);
	    builder.addRelations(relation2);
	    Relation relation3 = builder.build();
	}

	private Planner getPlanner() throws Exception {
		return Frameworks.getPlanner(config().build());
	}

	public Frameworks.ConfigBuilder config() throws Exception {
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

	public static final String TPCDS_MODEL = "{\n" + "  version: '1.0',\n" + "  defaultSchema: 'TPCDS',\n"
			+ "   schemas: [\n" + schema("TPCDS", "1.0") + ",\n" + schema("TPCDS_01", "0.01") + ",\n"
			+ schema("TPCDS_5", "5.0") + "\n" + "   ]\n" + "}";
	
	public String toString(RelNode rel, String format) {
	    return Util.toLinux(
	        RelOptUtil.dumpPlan("", rel, format.equals("json")? SqlExplainFormat.JSON: SqlExplainFormat.TEXT,
	            SqlExplainLevel.ALL_ATTRIBUTES));
	  }
	
	public String toString(RelNode rel) {
	    return Util.toLinux(
	        RelOptUtil.dumpPlan("", rel, SqlExplainFormat.JSON,
	            SqlExplainLevel.ALL_ATTRIBUTES));
	  }
}
