import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Crawler4BD {
	private static int n;// 搜索页数
	private static String keyW;// 搜索词
	private static String format = "";// 搜索结果页面的URL模板
	private static ArrayList<String> eachurl = new ArrayList<String>();// 用于保存链接
	private static Scanner scan;

	public static void main(String[] args) throws Exception {
		scan = new Scanner(System.in); 
		System.out.println("输入搜索页数及关键词");
		if(scan.hasNext()){  
			n = scan.nextInt();
        	keyW = scan.next();
        } 
		mainFunction(n, keyW);
	}

	public static void mainFunction(final int n, final String keyWord) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int x = n;// 页数
				Elements PageURLs = null;
				System.out.println("要提取百度关于“" + keyWord + "”搜索结果的前" + x + "页");
				
				try {
					mimicSearch(keyWord);
				} catch (FailingHttpStatusCodeException | IOException e) {
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
	public static void mimicSearch(String KeyWord) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// 创建Web Client
		// 模拟提交搜索词
		HtmlPage firstBaiduPage;
		
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(false);// HtmlUnit对JavaScript的支持不好，关闭之
		webClient.getOptions().setCssEnabled(false);// HtmlUnit对CSS的支持不好，关闭之
		HtmlPage page = (HtmlPage) webClient.getPage("http://www.baidu.com/");// 百度搜索首页页面
		HtmlInput input = (HtmlInput) page.getHtmlElementById("kw");// 获取搜索输入框并提交搜索内容（查看源码获取元素名称）
		input.setValueAttribute(KeyWord);// 将搜索词模拟填进百度输入框（元素ID如上）
		HtmlInput btn = (HtmlInput) page.getHtmlElementById("su");// 获取搜索按钮并点击
		firstBaiduPage = btn.click();// 模拟搜索按钮事件
		
		// 获取模板
		String morelinks = firstBaiduPage.getElementById("page").asXml();
		org.jsoup.nodes.Document pagedoc = Jsoup.parse(morelinks);
		Elements pagelinks = pagedoc.select("a[href]");
		Element newpage = pagelinks.first();
		String linkHref = newpage.attr("href");// 将提取出来的<a>标签中的链接取出
		format = "http://www.baidu.com" + linkHref;// 补全模板格式		
		
		return;
	}
	
	/*
	 * 获取搜索结果页的结果单元内的链接
	 */	
	public static Elements getPageUrls(int PageID) throws IOException{
		String url = format.replaceAll("&pn=1", "&pn=" + PageID + "");// 根据已知格式修改生成新的一页的链接
		//System.out.println("该页地址为：" + url);
		String USER_AGENT ="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";
        Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
        Document doc = connection.get();

        //Elements links = doc.select("a[data-click]");// 摘取该页搜索链接
        Elements links = doc.getElementsByClass("result");
        links = links.select("h3[class]").select("a[data-click]");
        System.out.println("#########"+links.size()+"############");
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
			//System.out.println("##########Text："+linkText+"###############\n"+linkHref);		
			if (linkHref.length()>0){//linkText.length() > 4 && linkText.contains(keyW)) {// 去除某些无效链接
				System.out.println("标题："+linkText + "\n链接：" + linkHref);
				eachurl.add(linkHref);
			}
		}
	}

}