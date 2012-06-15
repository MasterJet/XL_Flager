
package mySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import misc.*;
import proxy.Proxy;
import users.User;
import listing.Listing;
public class  MysqlDB 
{
	Connection con ;
	ConstVars StaticVars = new ConstVars();
	Misc misc = new Misc();
	
	public MysqlDB () throws SQLException, ClassNotFoundException
	{
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/"+StaticVars.DataBaseName,StaticVars.DataBaseUser,StaticVars.DataBasePass);
	}
	
	public int numRows(ResultSet result) throws SQLException
	{
		result.last();
		int r = result.getRow();
		result.beforeFirst();
		return r;
	}
	
	public ResultSet selectQ(String sql) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement(sql);
		ResultSet result  = statement.executeQuery();
		return result;
	}
	
	public Proxy getProxy() throws SQLException
	{
		ResultSet result  = selectQ("SELECT * from " + StaticVars.ProxyTable + " where weight > 0 Order by lastUsed limit 1");
		int numRows = numRows(result);
		if(numRows > 0)
		{
			result.first();
			Proxy proxy = new Proxy();
			proxy.proxy = result.getString("proxy");
			proxy.id = result.getInt("id");
			
			
			return proxy;
		}
		return null;
		
	}
	
	public User loadUser(int mid) throws SQLException
	{
		ResultSet userRecd ;
		ResultSet totalOfThisMaarket;
			
		String userSelectSQL = "Select * from users where type = 1 and mid = " + mid + " Order by lastUsed limit 1 "; // sql for select a user
		String TotalCheckSQL = "Select * from users where type = 1 and mid = " + mid ; // sql for total number of users associated to this market.
		totalOfThisMaarket  = selectQ(TotalCheckSQL);
		int numRows = numRows(totalOfThisMaarket);
		
		if(numRows < 5) // if no user associated to this market
		{
			PreparedStatement asignMID = con.prepareStatement("update users set mid = " + mid + " where type = 1 and mid = 0 limit 1"); // bind a user to this market.
			asignMID.execute();
		}
		userRecd  = selectQ(userSelectSQL); // select user of this market.
		if(numRows(userRecd) < 1)
		{
			userRecd = selectQ("Select * from users where type = 0  Order by lastUsed limit 1 ");
		}	
		
		User userObj = new User();
		userRecd.next();
		
		userObj.id = userRecd.getInt("id");
		userObj.mid = userRecd.getInt("mid");
		userObj.userName = userRecd.getString("username");
		userObj.password = userRecd.getString("password");
		userObj.browser = userRecd.getString("Browser");
		userObj.InterestID = userRecd.getInt("interestID");
		
		return userObj;
		
	}

	
	public User loadRandomUser() throws SQLException
	{
		ResultSet userRecd ;	
		String userSelectSQL = "Select * from users where type = 0  Order by lastUsed limit 1 "; // sql for select a user
		userRecd  = selectQ(userSelectSQL); // select user of this market.
		
		User userObj = new User();
		userRecd.next();
		
		userObj.id = userRecd.getInt("id");
		userObj.mid = userRecd.getInt("mid");
		userObj.userName = userRecd.getString("username");
		userObj.password = userRecd.getString("password");
		userObj.browser = userRecd.getString("Browser");
		
		return userObj;
		
	}

	public Listing[] loadAds(int marketid) throws SQLException
	{
		boolean portion = false;
		ResultSet listingRES2 = null;
		ResultSet listingRES;
		String sql1;
		if( marketid == 0 )
		{	
			sql1 = "Select id,url from " +StaticVars.ListingTable + " where killed = 0 order by lastUsed limit " + StaticVars.flagLimit;
		}
		else
		{	
			sql1 = "Select id,url from " + StaticVars.ListingTable + " where marketID = " + marketid + " and killed = 0  Order by lastUsed Limit " + StaticVars.flagLimit;
		}
		listingRES = selectQ(sql1);
		int totalRes = numRows(listingRES);
		
		
		if( totalRes < StaticVars.flagLimit )
		{
			int remaing = StaticVars.flagLimit - totalRes ;
			listingRES2 = selectQ("select id,url from " + StaticVars.ListingTable + " where killed = 0 order by lastUsed limit " + remaing);
			portion = true;
		}	
		
		Listing[] listings = new Listing[StaticVars.flagLimit] ;
		int i = 0 ;
		if( totalRes > 0 )
		{ 
			while(listingRES.next())
			{
				listings[i]= new Listing();
				listings[i].listingID = listingRES.getInt("id");
				listings[i].url = listingRES.getString("url"); 
				i++;
			}	
		}
		if(portion)
		{
			while(listingRES2.next())
			{
				
				listings[i]= new Listing();
				listings[i].listingID = listingRES2.getInt("id");
				listings[i].url = listingRES2.getString("url"); 
				i++;
			}	
		}
		return listings;
		
	}
	
	public void UpdateProxyUsed(int pid) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement("Update proxy set lastUsed = " + misc.getUnixTimeStamp() + " where id = " + pid);
		statement.execute(); 
	}
	
	public void DeleteProxy(int pid) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement("DELETE from  proxy  where id = " + pid);
		statement.execute(); 
	}
	
	public void ListingKilled(int lid) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement("update " + StaticVars.ListingTable + " SET killed  = 1 where id = " + lid);
		statement.execute(); 
	}
	
	public void Flaged(int lid) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement("update " + StaticVars.ListingTable + " SET flag  = flag + 1 , lastUsed = " + misc.getUnixTimeStamp() + " where id = " + lid);
		statement.execute(); 
	}
	
	public void UpdateUserLastUsed(int uid) throws SQLException
	{
		PreparedStatement statement = con.prepareStatement("Update " + StaticVars.UserTable + " set lastUsed = " + misc.getUnixTimeStamp() + " where id = " + uid);
		statement.execute(); 
	}
	
	public int getMarketId(String marketTitle) throws SQLException
	{
		ResultSet res = selectQ("select id from markets where marketTitle = '" + marketTitle + "'");
		int t = numRows(res);
		if(t > 0)
		{
			res.next();
			return res.getInt("id");
		}
		else
		{
			return 0 ;
		}	
	}
	
}
