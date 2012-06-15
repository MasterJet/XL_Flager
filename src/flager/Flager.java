package flager;

import java.sql.SQLException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import mySQL.MysqlDB;
import proxy.Proxy;
import users.User;
import listing.Listing;
import misc.*;
public class Flager {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		User user = new User();
		Proxy proxy = new Proxy();
		MysqlDB db = new MysqlDB();
		Misc functions = new Misc();		
		ConstVars constantVars = new ConstVars();
		DesiredCapabilities BrowserCapability ; // to hold capabilities object having working proxy.	
		
		Listing[] listings = new Listing[constantVars.flagLimit];
		
		while(true) // parent while	
		{
			proxy = db.getProxy(); functions.Echo(proxy.proxy);
			BrowserCapability = functions.capabilities(proxy.proxy);
			WebDriver TestingBrowser = new FirefoxDriver(BrowserCapability); //InternetExplorerDriver(BrowserCapability);
			
			int marketID = 0; // market id, We have 413 markets in Database.
			int ProxyType = 0; // 0 for unrecognized proxy and 1 for proxy that was recognized by CL.
			
			/********************************************* Proxy Testing ********************************************/
			try
			{
				TestingBrowser.get("http://craigslist.org");
				String pageTitle = TestingBrowser.getTitle();
				
				if(pageTitle.indexOf("craigslist:") == -1 ) //CL could not recognize IP 
				{	
					if(pageTitle.indexOf("Craigslist >") == -1) // Neither proxy is working
					{
						if(pageTitle.indexOf("ERROR") != -1 || pageTitle.indexOf("Error") != -1 || pageTitle.indexOf("403 Forbidden") != -1)
						{
							functions.Echo(proxy.proxy + " Bad Proxy -> " + pageTitle + " Removing Proxy from DB." );
							db.DeleteProxy(proxy.id);
							TestingBrowser.close();
						}
						else
						{
							functions.Echo(proxy.proxy + " Bad Proxy -> " + pageTitle + " Weight Decrement." );
							db.UpdateProxyUsed(proxy.id);
							TestingBrowser.close();
						}	
						continue;
					}
					else // on CL main sites page.
					{
						functions.Echo("Using " + proxy.proxy + " Unrecognized Proxy by CL."); 
						TestingBrowser.close();
					}
				}
				else
				{
					// We are here because CL recognized this Proxy.
					functions.Echo("Using " + proxy.proxy + " Proxy Recognized Proxy by CL.");
					WebElement marketTitle = TestingBrowser.findElement(By.tagName("h2")); 
					String Mtitle = marketTitle.getText(); functions.Echo("CL redirect to " + Mtitle); 
					marketID = db.getMarketId(Mtitle);   functions.Echo("market Id is  " +marketID);
					ProxyType = 1;
					TestingBrowser.close();
				}	
			}
			catch(org.openqa.selenium.NoSuchElementException e)
			{
				functions.Echo("Proxy Error ( "+ proxy.id + " => " + proxy.proxy + " )");
				TestingBrowser.close(); // close testing browser.
				db.UpdateProxyUsed(proxy.id); // update proxy used time, so that we can use next proxy. 
				continue; // come with next proxy.
			}
			
			
			/********************************************* Proxy Testing end  ***************************************/
			
			
			/******************************* Load User and Ads On the base of proxy *********************************/
			
			if( ProxyType == 0 )  // if proxy is not recognized by CL load random user and ads.
			{
				user = db.loadRandomUser();
				listings = db.loadAds(0);
			}
			else   // else load user and ads according to this location.
			{
				user = db.loadUser(marketID);
				listings = db.loadAds(marketID);
			}	
			
			functions.Echo("Loaded User ID: " + user.id);
			/******************************* Load User and Ads On the base of proxy *********************************/
			
			boolean loggedIn = false;
			
			WebDriver UserBrowser = null ;
			for(int i = 1; i <= 3; i++) // 3 attempts to login.
			{
				/**
				 * Load user's preferred browser
				 * A switch should be used but encounter this error
				 * Cannot switch on a value of type String for source level below 1.7. Only convertible int values or enum constants are permitted
				 * using firefox in both conditions.
				 */
				
				if(user.browser == "ff")
					UserBrowser = new FirefoxDriver(BrowserCapability);	
				else
					UserBrowser = new FirefoxDriver(BrowserCapability); //InternetExplorerDriver();
				
				try
				{
					UserBrowser.get("http://craigslist.org");
					
					try
					{
						WebElement myAccountLink = UserBrowser.findElement(By.xpath(constantVars.myAcountXpath));
						myAccountLink.click();
					}
					catch(org.openqa.selenium.NoSuchElementException e)
					{
						UserBrowser.get("https://accounts.craigslist.org");
					}
					
					WebElement loginEmail = UserBrowser.findElement(By.xpath(constantVars.loginEmailXpath));
					WebElement loginPass = UserBrowser.findElement(By.id(constantVars.loginPasswordID));
					WebElement loginButton = UserBrowser.findElement(By.xpath(constantVars.loginButtonXpath));
					loginEmail.sendKeys(user.userName);
			        loginPass.sendKeys(user.password);functions.Echo("Filling Login form.");
			        loginButton.click();
			        
			        WebElement toMarket = UserBrowser.findElement(By.xpath(constantVars.loginPagetoMarket));
			        toMarket.click();
				}
				catch(org.openqa.selenium.NoSuchElementException e) // any exception retry 3 times
				{
					functions.Echo("Login Attempt # "+i+ " failed.");
					UserBrowser.close();
					continue;
				}
				// if logged in break this loop.
				loggedIn = true;
				break;
			}
			
			/**
			 * If could not login after 3 attempts go back and use next proxy. 
			 */
			if(!loggedIn)
			{
				db.UpdateProxyUsed(proxy.id);
				functions.Echo("Could Not Login. Changing Proxy.");
				continue;
			}
			
			/**
			 * User is Logged in
			 */
			functions.Echo("Logged in");
			functions.pause(8);
			
			/**
			 * loop for every listing, 
			 * A limit for maximum listing to be loaded is defined in misc.ConstVars as ( public  final int flagLimit = 6; )
			 * 
			 */
			
			for(int j = 0 ; j < listings.length; j++)
			{
				int ListingNumber = j + 1; // only for use in writing logs
				String ListingTitleValue = null;
				int ListingID = 0;
				
				/**
				 * we have completed large process so we must not go back to starting point on an exception
				 * so give 3 chances to complete a flagging process.  
				 */
				
				for( int k = 0 ; k < 3; k++)
				{	
					try
					{
						/**
						 * break down listing url into pieces
						 * visit every page and maintain cookies
						 */
						String adUrl = listings[j].url;
						ListingID = listings[j].listingID;
						int needle1 = adUrl.lastIndexOf("org");
						int needle2 = adUrl.lastIndexOf("/");
						String u1 = adUrl.substring(0,needle1);
						String u2 = adUrl.substring(0,needle2);
				
						UserBrowser.get(u1 +"org"); functions.Echo("Listing # " + ListingNumber + ": Visiting Market page."); // visit main market page
						functions.pause(5);
						
						/**
						 * some random browsing and search functionality could be added here 
						 * for the sack of speed it is omitted for the time.
						 */
						
						UserBrowser.get(u2);   functions.Echo("Listing # " + ListingNumber + ": Visiting category page."); // visit category page
						functions.pause(5);
						UserBrowser.get(adUrl);  functions.Echo("Listing # " + ListingNumber + ": Visiting Listing page."); // visit Listing page
				
						WebElement ListingTitle = UserBrowser.findElement(By.xpath(constantVars.ListingTitleXpath));   // get title of listing
				
						ListingTitleValue = ListingTitle.getText();
						functions.Echo(ListingTitleValue); 
						functions.Echo("Listing # " + ListingNumber + ": A little pause before flag."); 
						functions.pause(5);
					}
					catch(org.openqa.selenium.NoSuchElementException e)
					{
						continue;
					}
					break; // if all pages visited successfully break 3 attempts loop 
				} //inner for loop
				
					// if listing has deleted, Expired or Flagged for removal Update in database ( killed = 1 )
					if(ListingTitleValue.indexOf("This posting has") != -1 )
					{	
						db.ListingKilled(ListingID);
					}
					else // else find miscategorized flag link and click on it 
					{
						try{
								WebElement miscategorized = UserBrowser.findElement(By.id(constantVars.miscategorizedLink));
								miscategorized.click();
								functions.pause(3);
								db.Flaged(ListingID);
						  }
						  catch(org.openqa.selenium.NoSuchElementException e)
						  {
							  continue;
						  } 
					}
					
					functions.Echo("Listing # " + ListingNumber + ": Flagged."); // update database listing flag time
					functions.pause(5);
			} //for every loaded listing
			
			db.UpdateProxyUsed(proxy.id); // update database proxy lastUsed time
			db.UpdateUserLastUsed(user.id); // and user LastUsed time
			
			/**
			 * Open admin page and logout
			 */
			try
			{
					UserBrowser.get("https://accounts.craigslist.org/");
					WebElement LogoutLink = UserBrowser.findElement(By.xpath(constantVars.logoutXpath));
					LogoutLink.click();
					functions.Echo("Logout");
			}
			catch(org.openqa.selenium.NoSuchElementException e)
			{
				functions.Echo("User admin page could't be loaded properly. closing browser.");
			}
				functions.pause(5);
				UserBrowser.close(); // close browser.
			
		} // parent while(true)	

	} // main

} // class
