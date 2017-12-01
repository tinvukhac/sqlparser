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
	    System.out.println(main.toString(rel, format));
		
//		String sql1 = "select * from customer where c_customer_id > 10";
//		String sql2 = "select dt.d_year ,item.i_brand_id brand_id ,item.i_brand brand ,sum(ss_ext_sales_price) sum_agg from date_dim dt ,store_sales ,item where dt.d_date_sk = store_sales.ss_sold_date_sk and store_sales.ss_item_sk = item.i_item_sk and item.i_manufact_id = 436 and dt.d_moy=12 group by dt.d_year ,item.i_brand ,item.i_brand_id order by dt.d_year ,sum_agg desc ,brand_id limit 100";
//		String sql3 = "select i_item_id, avg(ss_quantity) agg1, avg(ss_list_price) agg2, avg(ss_coupon_amt) agg3, avg(ss_sales_price) agg4 from store_sales, customer_demographics, date_dim, item, promotion where store_sales.ss_sold_date_sk = date_dim.d_date_sk and store_sales.ss_item_sk = item.i_item_sk and store_sales.ss_cdemo_sk = customer_demographics.cd_demo_sk and store_sales.ss_promo_sk = promotion.p_promo_sk and cd_gender = 'F' and cd_marital_status = 'W' and cd_education_status = 'Primary' and (p_channel_email = 'N' or p_channel_event = 'N') and d_year = 1998 group by i_item_id order by i_item_id limit 100";
//		SqlNode parse = planner.parse(sql3);
//		SqlNode validate = planner.validate(parse);
//	    RelNode rel = planner.rel(validate).project();
//	    System.out.println(test.toString(rel));
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
}
