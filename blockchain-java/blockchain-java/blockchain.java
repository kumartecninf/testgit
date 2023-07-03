package com.simplilearn.mavenproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import org.apache.logging.log4j.Logger;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
//import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.endpoints.BooleanResponse;

//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;


/**
 *  Blockchain Scraper main class
 *
 */
public class BlockchainScraper
{
	public static String m_version = "1.0";

	public static String m_listTableName;
	public static String m_APIListTableName;
	public static String m_ddbAccesskey; ;
	public static String m_ddbSecretAccesskey;
	public static String m_region;
	public static String m_hostDomain;
	public static String m_indexName;
	public static String m_tsFormat ;
	public static String m_maxSize ;

	static boolean m_indexExistCheck = false ;

    public static void main( String[] args ) throws Exception
    {
    	resultInfo data = new resultInfo() ;

    	try
    	{
    		System.out.println("BlockChain Scraper ver. " + m_version) ;

    		String iniFileName	= "./appConfig.ini";
    		FileInputStream input = new FileInputStream(iniFileName) ;

			Properties p = new Properties();
			p.load(input);

			m_listTableName = p.getProperty("LIST_TABLE_NAME");
			m_APIListTableName = p.getProperty("API_TABLE_NAME");
			m_ddbAccesskey = p.getProperty("DDB_ACCESS_KEY_ID") ;
			m_ddbSecretAccesskey = p.getProperty("DDB_SECRET_ACCESS_KEY") ;
			m_region = p.getProperty("REGION") ;
			m_hostDomain= p.getProperty("AWS_DOMAIN") ;
			m_indexName = p.getProperty("OS_INDEX_NAME") ;
			m_tsFormat = p.getProperty("TSFORMAT") ;

			String logLevelStr = p.getProperty("LOG_LEVEL") ;

			HashMap<String, Integer> levelMap = new HashMap<>();
			levelMap.put("ERROR", resultInfo.ERROR);
			levelMap.put("INFO", resultInfo.INFO);
			levelMap.put("DEBUG", resultInfo.DEBUG) ;

			if (levelMap.containsKey(logLevelStr))
			{
				data.m_loglevel = levelMap.get(logLevelStr);
			}

			input.close();

			data.trace(resultInfo.INFO, "Init done!");

			BasicAWSCredentials awsCreds = new BasicAWSCredentials(m_ddbAccesskey, m_ddbSecretAccesskey);
			AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.standard()
					.withRegion(m_region)
					.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

			ListTablesRequest request;

			boolean more_tables = true;
			String last_name = null;

			boolean listTableFound = false ;
			boolean APITableFound = false ;

			while(more_tables)
			{
				if (last_name == null)
				{
					request = new ListTablesRequest().withLimit(10);
				}
				else
				{
					request = new ListTablesRequest()
							.withLimit(10)
							.withExclusiveStartTableName(last_name);
				}				if (last_name == null)
				{
					request = new ListTablesRequest().withLimit(10);
				}
				else
				{
					request = new ListTablesRequest()
							.withLimit(10)
							.withExclusiveStartTableName(last_name);
				}

				ListTablesResult table_list = ddbClient.listTables(request);
				List<String> table_names = table_list.getTableNames();

				if (table_names.size() > 0)
				{
					for (String cur_name : table_names)
					{
						if (cur_name.equals(m_listTableName))
							listTableFound = true ;
						else
						{
							if (cur_name.equals(m_APIListTableName))
								APITableFound = true ;
						}
					}
				}
				else
				{
					data.raiseError("No tables found!");
					System.exit(0);
				}

				last_name = table_list.getLastEvaluatedTableName();
				if (last_name == null)
				{
					more_tables = false;
				}
			}

			if (APITableFound)
			{
				QueryRequest apiQueryReq = new QueryRequest().withTableName(m_APIListTableName)
			            .withKeyConditionExpression("enabled = :pk")
			            .addExpressionAttributeValuesEntry(":pk", new AttributeValue().withS("YES"));
			    QueryResult res = ddbClient.query(apiQueryReq);
			    List<Map<String, AttributeValue>> APIListItems = res.getItems();

			    ArrayList<String> apiList = new ArrayList<apiInfo>() ;
			    for (int i = 0; i < APIListItems.size(); i++)
		    	{
		    		Map<String, AttributeValue> rec = APIListItems.get(i) ;
		    		AttributeValue val = rec.get("URL") ;
		    		if (val != null)
		    		{
			    		apiInfo api = new apiInfo() ;
			    		api.m_URL = val.getS() ;

			    		AttributeValue val = rec.get("key") ;

			    		if (val != null)
		    			{
							api.m_key = val.getS() ;
						}

			    		apiList.add(api) ;
		    		}
		    	}

				if (listTableFound)
				{
				    QueryRequest queryRequest = new QueryRequest().withTableName(m_listTableName)
				            .withKeyConditionExpression("enabled = :pk")
				            .addExpressionAttributeValuesEntry(":pk", new AttributeValue().withS("YES"));
				    QueryResult result = ddbClient.query(queryRequest);
				    List<Map<String, AttributeValue>> walletList = result.getItems();

				    if (walletList == null || walletList.size() == 0)
				    {
				    	data.raiseError("wallet list table is empty");	;
				    }
				    else
				    {
				    	for (int i = 0; i < walletList.size() && !data.m_hasError; i++)
				    	{
				    		Map<String, AttributeValue> rec = walletList.get(i) ;
				    		AttributeValue val = rec.get("walletID") ;
				    		if (val != null)
				    		{
					    		String ID = val.getS() ;

					    		LoadWalletData(ID, apiList, data) ;
				    		}
				    	}
				    }
				}
				else
				{
					data.raiseError("Wallet list table not found");
				}
			}
			else
			{
				data.raiseError("API table not found");
			}
    	}
    	catch (Exception e)
    	{
    		data.raiseError(e.getMessage());
		}
    }

    public static void LoadWalletData(String walletID, ArrayList<String> apiList, resultInfo data)
    {

		if (data.m_hasError)
			return ;

    	try
    	{
			for (int i = 0; i < apiList.size() && !data.m_hasError; i++)
			{
				// here you API calls and fetching data

				....

				// update opensearch index

				updateDocumentIndex(walletID, jsontext, data);
			}
		}
		catch (Exception e)
		{
		    data.raiseError(e.getMessage());
		}
	}


	/* Update opensearch index */
	private static void updateDocumentIndex(String walletID, String JSONtext, resultInfo data)
	{
		if (data.m_hasError)
			return ;

    	try
    	{
    		JSONtext = JSONtext.trim() ;
			if (JSONtext.isEmpty())
				return ;

			int hash = text.hashCode() ;

			String timeStamp = getStringFromDate (null, m_tsFormat, data) ;

    		Region awsRegion = Region.of(m_region) ;

    		SdkHttpClient httpClient = ApacheHttpClient.builder().build();
            try {

                OpenSearchClient client = new OpenSearchClient(
                        new AwsSdk2Transport(
                                httpClient,
                                m_hostDomain,
                                awsRegion,
                                AwsSdk2TransportOptions.builder().build()));

                // create index on demand
                if (!m_indexExistCheck)
                {
                	BooleanResponse exist = client.indices().exists(ExistsRequest.of(s -> s.index(m_indexName)));
                	if (!exist.value())
                	{
                		CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder().index(m_indexName).build();
                		client.indices().create(createIndexRequest);
                	}
                	m_indexExistCheck = true ;
                }

                Map<String, Object> document = new HashMap<String, Object>();
                document.put("ID", walletID);
                document.put("ts", timeStamp);
                document.put(.... other data fron JSONtext ...);

                IndexRequest documentIndexRequest = new IndexRequest.Builder()
                        .index(m_indexName)
                        .id(walletID)
                        .document(document)
                        .build();
                client.index(documentIndexRequest);
            }
            finally
            {
                httpClient.close();
            }
    	}
    	catch (Exception e)
    	{
    		data.raiseError(e.getMessage());
		}
	}

	public static String getStringFromDate (java.util.Date date, String format, resultInfo data)
	{
		if (data.m_hasError)
			return ("") ;

		String dateStr	= "" ;

		try
		{
			if (date == null)
			{
				Calendar cal = Calendar.getInstance();
				date = cal.getTime();
			}

			DateFormat df	= new SimpleDateFormat(format);
			dateStr	= df.format(date);
		}
		catch(Exception e)
		{
			data.raiseError(e.getMessage());
		}

		return(dateStr) ;
	}
}
