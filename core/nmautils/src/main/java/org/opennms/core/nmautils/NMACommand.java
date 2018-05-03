package org.opennms.core.nmautils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSession;

import org.apache.commons.codec.binary.Base64;

public class NMACommand {
	 protected final static String GET_METHOD = "GET";
	  protected final static String POST_METHOD = "POST";

	  protected final static String CHARSET = "charset=";
	  protected final static String ISO_8859_1 = "ISO-8859-1";
	  private final static String UTF8 = "UTF-8";

	  private final static String LOCALHOST = "localhost";
	  private final static String LOCAL_IP = "127.0.0.1";

	  public final static String PLAINTEXT = "text/plain";
	  public final static String TEXTHTML = "text/html";
	  public final static String APPLICATION_XML = "application/xml";
	  public final static String APPLICATION_FORM = "application/x-www-form-urlencoded";
	  private String contentType = APPLICATION_FORM; //defalut value

	  protected final static int MAXCHARPERLINE = 1000;
	  
	protected String protocol = "HTTPS";
	protected String host = null;
	protected int port = 0;
	protected StringBuffer command = null;
	protected StringBuffer param = new StringBuffer();
	protected String data = null;
	private boolean useDefaultPassword = true;

	private String password = "abc123";
    
	public NMACommand(String host, String command, int port) {
		this.host = host;
		this.port = port;
		this.command = new StringBuffer("/cgi-bin/" + command);
	} 
	
	public void changeCommand(String command) {
		this.command = new StringBuffer(command);

	}

	public void setUseDefaultPassword(boolean useDefaultPassword) {
		this.useDefaultPassword = useDefaultPassword;
	}

	public NMAResponse execute() throws IOException {
		NMAResponse response = execute(GET_METHOD);
		return response;
	}

	public NMAResponse execute(String method) throws IOException {
		NMAResponse resp = null;
		HttpURLConnection conn = null;
		try {
			String authString = "admin" + ":" + password;
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringeEnc = new String(authEncBytes);

			URL url = new URL(protocol, host, port, getUrl(method));
			// System.out.println(url.toExternalForm());
			conn = (HttpURLConnection) url
					.openConnection(java.net.Proxy.NO_PROXY);
			if (conn instanceof HttpsURLConnection) {
				// https connection needs to configure SSL
				setupHttpsMode((HttpsURLConnection) conn);
			}
			conn.setRequestMethod(method);
			conn.setRequestProperty("Authorization", "Basic " + authStringeEnc);

		
			conn.setConnectTimeout(120* 60 * 1000);
			conn.setReadTimeout(120 * 60 * 1000);

			if (method.equals(GET_METHOD)) {
				conn.connect();
			} else if (method.equals(POST_METHOD)) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", getContentType());
				java.io.OutputStream os = conn.getOutputStream();
				os.write(getPostData().getBytes());
				os.flush();
				os.close();
			}

			if (conn.getResponseCode() != 200) {
				InputStream in = conn.getErrorStream();
				String errmsg = new InputStreamUtil(in, ISO_8859_1).toString();
				throw new IOException("NMA http error: " + errmsg);
			}

			String type = conn.getContentType();
			if (type.indexOf(PLAINTEXT) < 0 && type.indexOf(TEXTHTML) < 0) {
				throw new IOException(
						"NMA response content-type must be 'text/plain' or 'text/html'");
			}

			int index = type.indexOf(CHARSET);
			if (index < 0) {
				throw new IOException(
						"NMA response does not have 'charset' set");
			}
			String charset = type.substring(index + CHARSET.length());

			InputStream content = (InputStream) conn.getContent();
			String msg = new InputStreamUtil(content, charset).toString();
			resp = new NMAResponse(msg);
			if (NMAResponse.FAILURE.equals(resp.getStatus())
					&& url.getPath().startsWith("/cgi-bin/uninstall_"))
				resp.setDetail(msg);
		} catch (IOException ex) {
			 {
				//logger.error(ex.getMessage());
			} 
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		return resp;

	}

	protected String getUrl(String method) {
		String url = command.toString();
		String postData = getData();
		if (GET_METHOD.equals(method)
				|| (postData != null && postData.trim().length() > 0)) {
			url = url + "?" + param.toString();
		}
		return url;
	}

	 private String getPostData(){
		    String postData = getData();
		    if (postData == null || postData.trim().length() == 0){
		      postData = param.toString();
		    }
		    return postData;
	 }
	 
	private String getData() {
		return this.data;
	}
	
	public String getContentType() {
	    return contentType;
	  }
	private void setupHttpsMode(HttpsURLConnection conn) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(null, trustAllCerts, new java.security.SecureRandom());
			conn.setSSLSocketFactory(context.getSocketFactory());
			conn.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

