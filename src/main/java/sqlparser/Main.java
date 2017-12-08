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
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import edu.ucr.cs.qpe.RelationProtos.Relation;
import edu.ucr.cs.qpe.RelationProtos.Relation.Builder;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String filename = args[0];

		String query = FileUtils.readFileToString(new File(filename));
		query = query.trim();
		query = query.substring(0, query.length() - 1);
		Parser parser = new Parser();
		String json = parser.toJsonString(query, 1);
		System.out.println(json);
		
//		Planner planner = parser.getPlanner();
//		SqlNode parse = planner.parse(query);
//		SqlNode validate = planner.validate(parse);
//		RelNode rel = planner.rel(validate).project();
//		String explainStr = parser.toString(rel, "text");
//		System.out.println(explainStr);
	}
}
