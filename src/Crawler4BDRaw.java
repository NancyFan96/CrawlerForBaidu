import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class Crawler4BDRaw {
	private static int n;// 搜索页数
	private static String keyW;// 搜索词
	private static String format = "http://www.baidu.com";// 搜索结果页面的URL模板
	private static ArrayList<String> eachurl = new ArrayList<String>();// 用于保存链接
	private static Scanner scan;

    static int Timeout = 100000;
	static String USER_AGENT ="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";

	
	public static void main(String[] args) throws Exception {
		scan = new Scanner(System.in); 
		System.out.println("输入搜索页数及关键词");
		if(scan.hasNext()){  
			n = scan.nextInt();
        	keyW = scan.next();
        } 
		mainFunction(n, keyW);
	}

	public static void mainFunction(final int n, final String keyWord) throws MalformedURLException, IOException {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int x = n;// 页数
				Elements PageURLs = null;
				System.out.println("要提取百度关于“" + keyWord + "”搜索结果的前" + x + "页");
				
				try {
					mimicSearch(keyWord);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for (int i = 1; i <= x; i++) {
					System.out.println("\n************百度搜索“" + keyW + "”第" + i + "页结果************");
					try {
						PageURLs = getPageUrls(i);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					doCrawler(PageURLs);
				}
				System.out.println("\n\n\n输出所有地址");
				for (String xx : eachurl) {
					System.out.println(xx);
				}
				return;
			}
		});
		thread.start();
	}

	/*
	 * 模拟提交关键字并获取搜索结果的URL模板
	 */
	public static void mimicSearch(String KeyWord) throws IOException {
		// 发送请求
		String url = format + "/s?wd=" + KeyWord;
        Connection connection = Jsoup.connect(url).userAgent(USER_AGENT).timeout(Timeout);
        Document doc = connection.get();

		// 获取模板
		Elements pagelinks = doc.getElementById("page").select("a[href]");
		Element newpage = pagelinks.first();
		String linkHref = newpage.attr("href");
		format += linkHref;// 补全模板格式				
	}
	
	/*
	 * 获取搜索结果页的结果单元内的链接
	 */	
	public static Elements getPageUrls(int PageID) throws IOException{
		String url = format.replaceAll("&pn=1", "&pn=" + PageID + "");
		Connection connection = Jsoup.connect(url).userAgent(USER_AGENT).timeout(Timeout);
        Document doc = connection.get();

        Elements links = doc.getElementsByClass("result");
        links = links.select("h3[class]").select("a[data-click]");
       // System.out.println("#########"+links.size()+"############");
		return links;
	}
	
	/*
	 * 抓取与关键字有关的链接
	 */
	public static void doCrawler(Elements URLsOnAPage){
		for(Element newlink:URLsOnAPage){
			//System.out.println(newlink);		
			String linkHref = newlink.attr("href");// 提取包含“href”的元素成分，JSoup实现内部具体过程
			String linkText = newlink.text();// 声明变量用于保存每个链接的摘要
			System.out.println("标题："+linkText + "\n链接：" + linkHref);
			eachurl.add(linkHref);			
		}
	}
}