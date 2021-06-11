import com.constellio.data.dao.dto.records.RecordId;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FixingIds {

	public static void main(String[] args) throws Exception {
		List<String> lines = FileUtils.readLines(new File("/Users/francisbaril/Downloads/recordIds.txt"), "UTF-8");

		Set<RecordId> goodIds = lines.stream().filter(id -> RecordId.isIntId(id)).map(id -> RecordId.id(id)).collect(Collectors.toSet());

		Set<String> badNumericIds = lines.stream().filter(id -> !RecordId.isIntId(id) && isNumeric(id)).collect(Collectors.toSet());

		Set<String> badStringIds = lines.stream().filter(id -> !RecordId.isIntId(id) && !isNumeric(id)).collect(Collectors.toSet());

		for (String badNumeric : badNumericIds) {

			RecordId expectedId = RecordId.id(Integer.valueOf(badNumeric));
			if (goodIds.contains(expectedId)) {
				System.out.println("Can not fix " + goodIds);
			} else {
				goodIds.add(expectedId);
			}

		}
		System.out.println("Finished");


	}

	private static boolean isNumeric(String s) {
		try {
			Integer.valueOf(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
