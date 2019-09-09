package com.jump_higher.classes;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.jump_higher.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.ByteArrayBuffer;

/**
 * @author Stav Bodik
 * This class used to communicate from application with PHP files on the server which connects to DB tables .
 * The main purpose is to update and receive information on DB server .  
 */
public class ServerService extends Service{

	private final static int GETRANKINTERVAL = 1000 * 60 * 1; 
	private Handler mHandler;
	
	// strings used to point to php files in the server
    private  final String SERVER_URL = "http://192.168.43.240";
    private  final String REGISTART_FILE_URL = SERVER_URL+"/register.php";
    private  final String UPLOAD_IMAGE_FILE =  SERVER_URL+"/upload_image.php";
    private  final String NUMBEROFUSERS =  SERVER_URL+"/getNumberOfUsers.php";
    private  final String EMAILEXISTCHECK =  SERVER_URL+"/isEmailExist.php";
    private  final String UPDAERANKURL =  SERVER_URL+"/update_rank_details.php";
    private  final String GETUSERRANK =  SERVER_URL+"/getUserRankByID.php";
    private  final String GETTOP50 =  SERVER_URL+"/getTop50Table.php";
    private  final String GETPROFILEIMAGEBIG =  SERVER_URL+"/UserProfilImages/big/";
    private  final String GETPROFILEIMAGESMALL =  SERVER_URL+"/UserProfilImages/small/";

    private  final String LOGIN =  SERVER_URL+"/t_account.php";

    // intent strings used for broadcast messaging
    private RequestParams params = new RequestParams();	
    private String intentActionRegister  = "android.intent.action.REGISTER";
    private String intentActionUploadImage  = "android.intent.action.UPLOAD_IMAGE";
    private String intentActionNumberOfUsers  = "android.intent.action.NUSERS";
    private String intentActionLogin  = "android.intent.action.LOGIN";
    private String intentImageFromServer  = "android.intent.action.SAVESERVER";
    private String intentRestoreAccount  = "android.intent.action.RESTORE";
    private String intentEmailExistCheck  = "android.intent.action.EMAIL";
    private String intentUpdateRank  = "android.intent.action.UPDATERANK";
    private String intentGetRankSequential   = "android.intent.action.GETRANK";
    private String intentGetTOP50   = "android.intent.action.TOP50";

    // local service binder , used to bind an activity to service . 
	private final LocalBinder mBinder = new LocalBinder();

	// LocalBinder, mBinder and onBind() allow other Activities to bind to this service.
	public class LocalBinder extends Binder {
	    public ServerService getService() {
	      return ServerService.this;
	    }
	  }
	
  @Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
    public void getImageFromServer(final int userID,final boolean bigImage,final boolean savetoDisk,final int viewID){
             
		new AsyncTask<Void, Void, String>() {

			
			
			@Override
			public String doInBackground(Void... params) {

				try {
					URL url;
					if(bigImage)
					url = new URL(GETPROFILEIMAGEBIG+userID+".png"); 
					else
                    url = new URL(GETPROFILEIMAGESMALL+userID+".png"); 
	
                    URLConnection ucon = url.openConnection();

                    InputStream is = ucon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);

                    /*
                     * Read bytes to the Buffer until there is nothing more to read(-1).
                     */
                    ByteArrayBuffer baf = new ByteArrayBuffer(50);

                    int current = 0;
                    while ((current = bis.read()) != -1) {
                            baf.append((byte) current);
                    }

                    byte[] imageData = baf.toByteArray();

                    
                    Bitmap profileImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    
                    if(savetoDisk){
                    ApplicationManager.getInstance().saveProfileImageToDisk(profileImage, Integer.toString(userID));
                    Intent i = new Intent(intentImageFromServer);
                    i.putExtra("CODE", 1);
                    sendBroadcast(i);
                    }else{
                    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    	profileImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    	byte[] byteArray = stream.toByteArray();
                        Intent i = new Intent(intentImageFromServer);
                        i.putExtra("CODE", 1);
                    	i.putExtra("image",byteArray);
                        i.putExtra("viewID", viewID);
                        sendBroadcast(i);
                    }
                    
            } catch (Exception e) {
           	 Intent i = new Intent(intentImageFromServer);
             i.putExtra("CODE", 0);
             i.putExtra("viewID", viewID);
             sendBroadcast(i);
            }
				
				
				
				
				return null;
			}

			
		}.execute(null, null, null);
		
 
    }
  
    public void login(String username, String password) {
        
    	username=username.replaceAll("\\s+", "_");

    	String urlSuffix;
    	try {
			username = URLEncoder.encode(username, "UTF-8");
			password =URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
		
		 urlSuffix = "?username="+username+"&password="+password;
		
		class RegisterUser extends AsyncTask<String, Void, String>{

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               
           }

           @Override
           protected void onPostExecute(String result) {
               super.onPostExecute(result);
        	   
               String s = result;
        	    Intent i = new Intent(intentActionLogin);
               
        	    if(s!=null){
        	    	
        	    	String res[] = result.split(",");
        	    	if(res[0].equals("1")){
	                   	    i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[11]));  
	 		                i.putExtra("CODE", Integer.parseInt(res[0]));
	 		                i.putExtra("USERINFO", s.replaceAll("_", " "));
	 		                sendBroadcast(i);
	         	    	}else{
	         	    		i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[1]));
	                  	    sendBroadcast(i);
	         	    }
        	    	
               }else{
               	sendBroadcast(i);
               }
               
               
           }

           @Override
           protected String doInBackground(String... params) {
        	   String s = params[0];
               BufferedReader bufferedReader = null;
               try {
            	   disableConnectionReuseIfNecessary();
            	   String request = LOGIN+s+"&?useUnicode=true&characterEncoding=UTF-8";
                   URL url = new URL(request);
                   HttpURLConnection con=(HttpURLConnection) url.openConnection();
                   bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                   String result = "";
                   for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                   {
                       result+=line;
                   }

                   result=result+","+con.getResponseCode();
                   
                   return result;
               }catch(Exception e){
                   e.printStackTrace();
                   return null;
               }
               
           }
       }

       RegisterUser ru = new RegisterUser();
       ru.execute(urlSuffix);
       
   }
    
    public void getNumberOfRegistartedUsers() {
    	
            
    		
   		
   		class numberOfUsers extends AsyncTask<String, Void, String>{

              @Override
              protected void onPreExecute() {
                  super.onPreExecute();
                  
              }

              @Override
              protected void onPostExecute(String s) {
                  super.onPostExecute(s);
           	      Intent i = new Intent(intentActionNumberOfUsers);
          	       

           	      if(s!=null){
             	   String[] res=s.split(",");
             	   i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[1]));
             	   i.putExtra("CODE", Integer.parseInt(res[0]));             	   
               	   sendBroadcast(i);
           	      }else{
           	    	i.putExtra("CODE", 0);
                	sendBroadcast(i);
           	      }
              }

              @Override
              protected String doInBackground(String... params) {
                  BufferedReader bufferedReader = null;
                  try {
                      URL url = new URL(NUMBEROFUSERS);
                      HttpURLConnection con = (HttpURLConnection) url.openConnection();
                      bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                      String result = "";
                      for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                      {
                          result+=line;
                      }
                      result=result+","+con.getResponseCode();

                      return result;
                  }catch(Exception e){
                      return null;
                  }
              }
          }

   		numberOfUsers ru = new numberOfUsers();
        ru.execute();
    }
    
    public void isUserEmailFoundOnDB(String userEmail){
    	
    	
    	String urlSuffix = null;
		try {
			urlSuffix = "?useremail="+URLEncoder.encode(userEmail, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		class RegisterUser extends AsyncTask<String, Void, String>{

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               
           }

           @Override
           protected void onPostExecute(String s) {
               super.onPostExecute(s);
        	   Intent i = new Intent(intentEmailExistCheck);
               if(s!=null){
	            	   String[] res=s.split(",");
	                   int respCode = Integer.parseInt(res[0]);
	                   int resultCode=Integer.parseInt(res[1]);
	            	   i.putExtra("HTTPRESPONE_CODE",respCode);
	            	   i.putExtra("CODE", resultCode);
	            	   
	            	   if(respCode==200){
	            		   if(resultCode==-1){
	            			   i.putExtra("MSG", getString(R.string.emailNotExist));
	            			   sendBroadcast(i);
	            		   }else{
	            			   i.putExtra("MSG", s);
	            			   sendBroadcast(i);
	            		   }
	            		   
	            	   }else {
	            		   i.putExtra("MSG", getString(R.string.internetproblem));
	            		   sendBroadcast(i);
	            	   }
	            	   
            	   
               }else{
            	   i.putExtra("MSG", getString(R.string.internetproblem));
            	   sendBroadcast(i);
               }

           }

           @Override
           protected String doInBackground(String... params) {
        	   String s = params[0];
               BufferedReader bufferedReader = null;
               try {
            	   disableConnectionReuseIfNecessary();
            	   String request = EMAILEXISTCHECK+s+"&?useUnicode=true&characterEncoding=UTF-8";
            	  // request=java.net.URLDecoder.decode(request, "UTF-8");
                   URL url = new URL(request);
                   HttpURLConnection con=(HttpURLConnection) url.openConnection();
                   
                   bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));


                   String result = "";
                   for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                   {
                       result+=line;
                   }


                   result = con.getResponseCode()+","+result;


                   return result;
               }catch(Exception e){
                   e.printStackTrace();
                   
                   return null;
               }
               
           }
       }

       RegisterUser ru = new RegisterUser();
       ru.execute(urlSuffix);
    	
    	
    	
    }
    public void restoreInformation(final String userEmail,final String userName,final String password){
    	
 
    	
    	new AsyncTask<Void, Void, String>() {

    		
    		protected void onPostExecute(String result) {

    		};
			@Override
			public String doInBackground(Void... params) {

				Properties props = new Properties();
	            props.put("mail.smtp.host", "smtp.gmail.com");
	            props.put("mail.smtp.socketFactory.port", "465");
	            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	            props.put("mail.smtp.auth", "true");
	            props.put("mail.smtp.port", "465");

	            Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication("jumphigherrestore@gmail.com", "q!w@e#r$A");
	                }
	            });

	            try {
	                Message message = new MimeMessage(session);
	                message.setFrom(new InternetAddress("jumphigherrestore@gmail.com"));
	                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
	                message.setSubject(getString(R.string.emailtitel));
	                message.setContent(getString(R.string.emailcontect1)+"<br>"+getString(R.string.emailcontent2)+"<br>"+getString(R.string.emailcontent3)+"<br>"+getString(R.string.emailcontent4)+userName+"<br>"+getString(R.string.emailcontent5)+password, "text/html; charset=utf-8");
	                Transport.send(message);
	                
	                Intent i = new Intent(intentRestoreAccount);
	                i.putExtra("CODE", 1);
	                sendBroadcast(i);
	                

	            } catch (Exception e) {
	            	Intent i = new Intent(intentRestoreAccount);
	                i.putExtra("CODE", 0);
	                sendBroadcast(i);	                
	            }
                    
            
				return null;
				
				
				
			}

			
		}.execute(null, null, null);
    	
    

    	
}
    
    public void getTOP50(boolean isMyRank,int userrank){
    	
    	String urlSuffix="?ismyrank=0&userrank=-1";
    	if(isMyRank)urlSuffix="?ismyrank=1&userrank="+userrank;

    	class numberOfUsers extends AsyncTask<String, Void, String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
         	      Intent i = new Intent(intentGetTOP50);
         	       
         	      if(s!=null){
         	    	 String[] res=s.split("@");
  	           	     i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[0]));
  	           	   
		           	   if(res[0].equals("200")){
		           		   i.putExtra("rankTable", res[1]);
		           		   sendBroadcast(i);
		           	   }else{
		           		   sendBroadcast(i);
		           	   }
         	      }else{
	           		   sendBroadcast(i);
         	      }
            }

            @Override
            protected String doInBackground(String... params) {
                BufferedReader bufferedReader = null;
         	    String s = params[0];

                try {
                    URL url = new URL(GETTOP50+s);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String result;

                    String ret="";
                    while ((result = bufferedReader.readLine()) != null){                     
                    ret+=result;
                    }

                    ret=con.getResponseCode()+"@"+ret;
                    
                    return ret;
                }catch(Exception e){
                    return null;
                }
            }
        }

 		numberOfUsers ru = new numberOfUsers();
        ru.execute(urlSuffix);
    	
    }
    
    public void register_update(String username, String password, String email,String country,boolean isUpdate,int userID) {
        
    	String urlSuffix;
    	try {
			username = URLEncoder.encode(username, "UTF-8");
			email =URLEncoder.encode(email, "UTF-8");
			password =URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
		 if(isUpdate)urlSuffix = "?username="+username+"&password="+password+"&email="+email+"&country="+country+"&isUpdate=1"+"&userid="+userID;
		 else
		 urlSuffix = "?username="+username+"&password="+password+"&email="+email+"&country="+country+"&isUpdate=0";
		
		class RegisterUser extends AsyncTask<String, Void, String>{

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               
           }

           @Override
           protected void onPostExecute(String s) {
               super.onPostExecute(s);
        	   Intent i = new Intent(intentActionRegister);
               if(s!=null){
            	   String[] res=s.split(",");
            	   i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[1]));
            	   i.putExtra("CODE", Integer.parseInt(res[0]));
            	   
	                if(res[0].equals("-1")){ 	
	                	i.putExtra("MSG", getString(R.string.useremailexist));
						sendBroadcast(i);
	                	
	                }else if(Integer.parseInt(res[0])>0){
	                	
						sendBroadcast(i);
	                    
	                }else if(res[0].equals("0")){
	                	i.putExtra("MSG", getString(R.string.internetproblem));
	                	sendBroadcast(i);
	                }
               }else{
            	    i.putExtra("MSG", getString(R.string.internetproblem));
				    sendBroadcast(i);
               }

           }

           @Override
           protected String doInBackground(String... params) {
        	   String s = params[0];
               BufferedReader bufferedReader = null;
               try {
            	   disableConnectionReuseIfNecessary();
            	   String request = REGISTART_FILE_URL+s+"&?useUnicode=true&characterEncoding=UTF-8";
            	   //request=java.net.URLDecoder.decode(request, "UTF-8");
                   URL url = new URL(request);
                   HttpURLConnection con=(HttpURLConnection) url.openConnection();
                   
                   bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                   String result = "";
                   for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                   {
                       result+=line;
                   }

                   result=result+","+con.getResponseCode();

                   return result;
               }catch(Exception e){
                   e.printStackTrace();
                   
                   return null;
               }
               
           }
       }

       RegisterUser ru = new RegisterUser();
       ru.execute(urlSuffix);
       
   }

    public void onStart(Intent intent, int startId) {
    	mHandler = new Handler();
    	mHandler.postDelayed(mHandlerTask, GETRANKINTERVAL);
    };
        
    Runnable mHandlerTask = new Runnable()
    {
         @Override 
         public void run() {
             
        	 if(ApplicationManager.getInstance().getLoggedInUser()!=null){
        	 USERRANKTASK getUserRank = new USERRANKTASK();
        	 getUserRank.execute("?userid="+ApplicationManager.getInstance().getLoggedInUser().getUserID());
        	 mHandler.postDelayed(mHandlerTask, GETRANKINTERVAL);
        	 }
         }
    };
	
    public class USERRANKTASK extends AsyncTask<String, Void, String>{

   @Override
   protected void onPreExecute() {
       super.onPreExecute();
       
   }

   @Override
   protected void onPostExecute(String s) {
       super.onPostExecute(s);
	   Intent i = new Intent(intentGetRankSequential);
	    if(s!=null){
	    	String res[] = s.split(",");
	    	if(res[0].equals("1")){
	                i.putExtra("USERINFO", s.replaceAll("_", " "));
	                sendBroadcast(i);
     	    	}
	    	
       }

   }

   @Override
   protected String doInBackground(String... params) {
	   String s = params[0];
       BufferedReader bufferedReader = null;
       try {
    	   disableConnectionReuseIfNecessary();
    	   String request = GETUSERRANK+s;
    	   request=java.net.URLDecoder.decode(request, "UTF-8");
           URL url = new URL(request);
           HttpURLConnection con=(HttpURLConnection) url.openConnection();
           
           bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

           String result = "";
           for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
           {
               result+=line;
           }
           result=result+","+con.getResponseCode();

               return result;
           }catch(Exception e){
               e.printStackTrace();
               
               return null;
           }
           
       }
   }

    public void updateRankonFinishRound(int userID,String rachedTime,int level) {
        
    	String urlSuffix = "?userid="+userID+"&bestusertime="+rachedTime+"&bestuserlevel="+level;
		
		class RegisterUser extends AsyncTask<String, Void, String>{

           @Override
           protected void onPreExecute() {
               super.onPreExecute();
               
           }

           @Override
           protected void onPostExecute(String s) {
               super.onPostExecute(s);
        	   Intent i = new Intent(intentUpdateRank);
        	    if(s!=null){
        	    	
        	    	String res[] = s.split(",");
        	    	if(res[0].equals("1")){
	                   	    i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[11]));  
	 		                i.putExtra("CODE", Integer.parseInt(res[0]));
	 		                i.putExtra("USERINFO", s.replaceAll("_", " "));
	 		                sendBroadcast(i);
	         	    	}else{
	         	    		i.putExtra("HTTPRESPONE_CODE", Integer.parseInt(res[1]));
	                  	    sendBroadcast(i);
	         	    }
        	    	
               }else{
               	sendBroadcast(i);
               }

           }

           @Override
           protected String doInBackground(String... params) {
        	   String s = params[0];
               BufferedReader bufferedReader = null;
               try {
            	   disableConnectionReuseIfNecessary();
            	   String request = UPDAERANKURL+s;
            	   request=java.net.URLDecoder.decode(request, "UTF-8");
                   URL url = new URL(request);
                   HttpURLConnection con=(HttpURLConnection) url.openConnection();

                   con.setConnectTimeout(1000);


                   bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                   String result = "";
                   for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                   {
                       result+=line;
                   }
                   result=result+","+con.getResponseCode();

                   return result;
               }catch(Exception e){
                   e.printStackTrace();
                   
                   return null;
               }
               
           }
       }

       RegisterUser ru = new RegisterUser();
       ru.execute(urlSuffix);
       
   }

	public void uploadUserImage(final Bitmap bitmap,final String imageName){
		
		new AsyncTask<Void, Void, String>() {

			private String encodedString;

			
			protected void onPreExecute() {

			};

			@Override
			protected String doInBackground(Void... params) {
				
				try{
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// Must compress the Image to reduce image size to make upload easy
				bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream); 
				byte[] byte_arr = stream.toByteArray();
				// Encode Image to String
				encodedString = Base64.encodeToString(byte_arr, 0);
				}catch(Exception e){}
				return "";
			}

			@Override
			protected void onPostExecute(String msg) {
				// Put converted Image string into Async Http Post param
				params.put("image", encodedString);
				params.put("filename", imageName);
				// Trigger Image upload
				makeHTTPCall();
			}
		}.execute(null, null, null);
	
	}
	
	private void disableConnectionReuseIfNecessary() {
	    // HTTP connection reuse which was buggy pre-froyo
	    if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
	        System.setProperty("http.keepAlive", "false");
	    }
	}
	
	public void makeHTTPCall() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(UPLOAD_IMAGE_FILE,
				params, new AsyncHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] arg1, byte[] arg2, Throwable arg3) {
			        	   Intent i = new Intent(intentActionUploadImage);
			        	   i.putExtra("CODE", statusCode);
			        	   i.putExtra("MSG", getString(R.string.internetproblem));
						   sendBroadcast(i);
					}

					@Override
					public void onSuccess(int statusCode, Header[] arg1, byte[] arg2) {			
						   Intent i = new Intent(intentActionUploadImage);
			        	   i.putExtra("CODE", statusCode);
						   sendBroadcast(i);


			}
		});
	}
	
    public void getUserRank(int userID){
		
	}
	
	public void setUserStats(int userID,int level,String reachedTime){
		
	}
	
	public void addLikeToRankList(int userID){
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}	
}