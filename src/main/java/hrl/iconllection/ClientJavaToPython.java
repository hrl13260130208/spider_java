package hrl.iconllection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import hrl.good.Information;

public class ClientJavaToPython {
	public static final String IP="127.0.0.1";
	public static final int PORT=8010;
	
	public static final String EXCEL_XLSX="xlsx";
	public static final String EXCEL_FILE_PATH="C:/python/workspace/python/timing_run/result.xlsx";
	
	/**
	 * 将server传回来的json解析成Information对象
	 * 
	 * @param string server传回来的json字符串
	 * @param website 对应爬取的网站名
	 * @return 包含Information的list
	 */
	private List<Information> parserJSON(String string,String website) {
		 JSONObject jsonObject = new JSONObject(string);
		 String date=(String)jsonObject.get("date");
		 String word=(String)jsonObject.get("word");
		 JSONArray  list=jsonObject.getJSONArray("msg");
		 Iterator iterator=list.iterator();
		 List<Information> lInformations=new ArrayList<Information>();
		 while (iterator.hasNext()) {
			JSONArray object = (JSONArray) iterator.next();
			Iterator oIterator=object.iterator();
			
			String url=(String)oIterator.next();
			String price=(String)oIterator.next();
			String title=(String)oIterator.next();
			
			Information information=new Information();
			information.setDate(date);
			information.setPrice(price);
			information.setTitle(title);
			information.setUrl(url);
			information.setWebsite(website);
			information.setWord(word);
			
			lInformations.add(information);
			
		}
		 
		 return lInformations;  
	}	
	
	/**
	 * 将封装好的list中的文件写入excel中
	 * 
	 * @param list
	 * @throws EncryptedDocumentException
	 * @throws IOException
	 */
	private void WriteToExcel(List<Information> list) throws EncryptedDocumentException, IOException {
		Iterator<Information> iterator=list.iterator();
		File file=new File(EXCEL_FILE_PATH);

        FileInputStream in = new FileInputStream(file);
        XSSFWorkbook wb=new XSSFWorkbook(in);
        
        Sheet sheet= wb.getSheetAt(0);
        int lastNum=sheet.getLastRowNum();
        System.out.println("原行数："+lastNum);
        int rowNum=lastNum+1;
        
        System.out.println("write ...");
		while (iterator.hasNext()) {
			FileOutputStream outputStream=null;
			try {
				Information information = (Information) iterator.next();
				Row row = sheet.createRow(rowNum);
				
				Cell cell = row.createCell(0);
				cell.setCellValue(rowNum);
				Cell cell2 = row.createCell(1);
				cell2.setCellValue(information.getWebsite());
				Cell cell3 = row.createCell(2);
				cell3.setCellValue(information.getWord());
				Cell cell4 = row.createCell(3);
				cell4.setCellValue(information.getPrice());
				Cell cell5 = row.createCell(4);
				cell5.setCellValue(information.getDate());
				Cell cell6 = row.createCell(5);
				cell6.setCellValue(information.getUrl());
				Cell cell7 = row.createCell(6);
				cell7.setCellValue(information.getTitle());
				
				outputStream = new FileOutputStream(file);
				wb.write(outputStream);
			} finally {
				if (outputStream!=null) {
					outputStream.close();
					rowNum++;
				}
			}
		}
		System.out.println("write complete!");
	}

	
	/**
	 * 将server返回的list写入excel
	 * 
	 * @param list
	 * @param website
	 * @throws EncryptedDocumentException
	 * @throws IOException
	 */
	private void write(List<String> list,String website) throws EncryptedDocumentException, IOException {
		
		Iterator<String> iterator=list.iterator();
		while (iterator.hasNext()) {
			String string2 = (String) iterator.next();
			WriteToExcel(parserJSON(string2, website));
		}
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {	
		ClientJavaToPython client=new ClientJavaToPython();
		ExecutorService executorService=Executors.newFixedThreadPool(4);
		JDClient jClient=client.new JDClient();
		TMClient tClient=client.new TMClient();
		
		Future<List<String>> future=executorService.submit(jClient);
		List<String> list=future.get();
		
		Future<List<String>> future2=executorService.submit(tClient);
		List<String> list2=future2.get();
		
		client.write(list, "京东");
		client.write(list2, "天猫");
		
		
	
	}
	
	
	/**
	 * 
	 * 连接京东的client
	 * 
	 * @author herenli
	 *
	 */
	class JDClient implements Callable<List<String>>{
		
		List<String> list=new ArrayList<String>();

		public List<String> call() throws Exception {
			Socket socket=null;
			
			try {
				socket=new Socket(IP, PORT);
				DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
				dataOutputStream.write("1".getBytes("utf-8"));
				DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
				byte[] b=new byte[1024*100];
				String string=null;
				while (true) {
					int backData=dataInputStream.read(b);
					if (backData==-1) {
						break;
					}
					System.out.println(backData);
					string=new String(b,"utf-8").trim();
					System.out.println(string);
					list.add(string);
				}
			} catch (Exception e) {
				System.err.println("connection has err!");
			}
			return list;
		}
	}

	
	/**
	 * 
	 * 连接天猫的client
	 * 
	 * @author herenli
	 *
	 */
	class TMClient implements Callable<List<String>>{
		
		List<String> list=new ArrayList<String>();

		public List<String> call() throws Exception {
			Socket socket=null;
			
			try {
				socket=new Socket(IP, PORT);
				DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
				dataOutputStream.write("2".getBytes("utf-8"));
				DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
				byte[] b=new byte[1024*100];
				String string=null;
				while (true) {
					int backData=dataInputStream.read(b);
					if (backData==-1) {
						break;
					}
					System.out.println(backData);
					string=new String(b,"utf-8").trim();
					System.out.println(string);
					list.add(string);
				}
			} catch (Exception e) {
				System.err.println("connection has err!");
			}
			return list;
		}
	}
	
}
