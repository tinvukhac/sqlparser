package sqlparser;

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

import com.google.common.base.Function;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Hello Apache Calcite");
		Main test = new Main();
		Planner planner = test.getPlanner();
		String sql1 = "select * from customer where c_customer_id > 10";
		String sql2 = "select i_item_desc ,i_category , i_current_price ,i_item_id ,sum(ws_ext_sales_price) as itemrevenue ,sum(ws_ext_sales_price)*100/sum(sum(ws_ext_sales_price)) as revenueratio from web_sales,item ,date_dim where web_sales.ws_item_sk = item.i_item_sk and item.i_category in ('Jewelry', 'Sports', 'Books') and web_sales.ws_sold_date_sk = date_dim.d_date_sk and date_dim.d_date between '2001-01-12' and '2001-02-11' group by i_item_id ,i_item_desc ,i_category ,i_current_price order by i_category ,i_item_id ,i_item_desc,revenueratio limit 100";
		SqlNode parse = planner.parse(sql1);
		System.out.println(parse.toString());
		SqlNode validate = planner.validate(parse);
	    RelNode rel = planner.rel(validate).project();
	    System.out.println(test.toString(rel));
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
	
	public String toString(RelNode rel) {
	    return Util.toLinux(
	        RelOptUtil.dumpPlan("", rel, SqlExplainFormat.JSON,
	            SqlExplainLevel.NO_ATTRIBUTES));
	  }
}
