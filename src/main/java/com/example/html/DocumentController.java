package com.example.html;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
public class DocumentController {


	@Value("${document-name}")
	private String documentName;

	
	@GetMapping("rowscolumnscount")
	public Count GetCount() throws IOException {

		File file = new File(documentName);
		Document doc = Jsoup.parse(file,null);
		
		Elements tables = doc.select("table");
		Elements columns = tables.select("th");
		Elements rows = tables.select("tbody").select("tr");
		
		Count count = new Count();
		count.setColumnCount(columns.size());
		count.setRowCount(rows.size());
		
		
		
		return count;
	}
	@GetMapping("fullData")
	public List<Table> GetAll() throws IOException {
		
		File file = new File(documentName);
		Document doc = Jsoup.parse(file,null);
		
		Elements tables = doc.select("table");
		Elements rows = tables.select("tbody").select("tr");
		
		List<Table> data = new ArrayList<Table>();
		
		for(Element row : rows) {
			Table table = new Table();
			table.setName(row.select("td").get(0).text().toString());
			table.setClass(row.select("td").get(1).text().toString());
			table.setBatch(row.select("td").get(2).text().toString());
			data.add(table);
		}
		
		return data;
		
	}
	
	@GetMapping("fullSortData")
	public List<Table> GetAllBySort() throws IOException {
		
		List<Table> data = this.GetAll();
		

		 Comparator<Table> nameComparator
	      = (h1, h2) -> h1.getName().substring(h1.getName().length()-1)
	      .compareTo(h2.getName().substring(h2.getName().length()-1));
	      
		 Comparator<Table> comparator = Comparator.comparing(Table::getBatch)
				 .thenComparing(Comparator.comparing(Table::getclass))
				 .thenComparing(nameComparator.reversed());
		 
	    
		List<Table> sortData = data.stream().sorted(comparator).collect(Collectors.toList());
		
		
		return sortData;
	}
	
	@GetMapping("groupData")
	public Map<String, CutomTable> GetAllByGroupSort() throws IOException {
		
		List<Table> data = this.GetAll();
		
		
		Map<String, List<Table>> groupData = data.stream()
				  .collect(Collectors.groupingBy(Table::getBatch));
		
		Map<String, CutomTable> map = new HashMap<>();
		 for (Map.Entry<String,List<Table>> entry : groupData.entrySet())  
		 {
			 CutomTable customTable = new CutomTable();
			 List<ChildTable> tables = new ArrayList<ChildTable>();
			 for(Table table : entry.getValue()) {
				 ChildTable t = new ChildTable();
				 t.setName(table.getName());
				 t.setClass( table.getclass());
				 tables.add(t);
			 }
			 
			 customTable.setMembers(tables);
			 customTable.setCount(tables.size());
			 map.put(entry.getKey(), customTable);
		 }
		
		
		return map;
	}
	
	@PostMapping("findByparam")
	public List<Table>  FilterData(@RequestBody FilterRequest request) throws IOException {


		List<Table> data = GetAll();
		
		
		List<Table> filteredData = data.stream()
				.filter(t-> t.getName().equals(request.getCol_1()) && t.getclass().equals(request.getCol_2())).collect(Collectors.toList());
		
		return filteredData;
		
	}
}
