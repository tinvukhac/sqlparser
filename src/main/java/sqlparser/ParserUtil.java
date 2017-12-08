package sqlparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;

public class ParserUtil {
	
	public static <T> void getAllOps(final Class<T> type) {
		Reflections reflections = new Reflections("org.apache.calcite");
		Set<Class<? extends T>> rels = reflections.getSubTypesOf(type);
		if(rels.size() == 0) {
			return;
		} else {
			for(Class<? extends T> rel: rels) {
				System.out.println(rel.getSimpleName());
				getAllOps(rel);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void storeAllOps() throws IOException {
		Set<String> allOps = new HashSet<String>();
		File file = new File("ops.txt");
		List<String> lines = FileUtils.readLines(file, "UTF-8");
		for(String line: lines) {
			allOps.add(line.trim());
		}
		List<String> deduplicatedOps = new ArrayList<String>();
		deduplicatedOps.addAll(allOps);
		deduplicatedOps.sort(String::compareToIgnoreCase);
		file = new File("final_ops.txt");
		int i = 0;
		for(String line: deduplicatedOps) {
			FileUtils.writeStringToFile(file, "\t\t" + line + " = " + i + ";\n", true);
			i++;
		}
	}
}
