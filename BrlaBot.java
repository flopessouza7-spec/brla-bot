import java.net.*; import java.io.*; import com.sun.net.httpserver.*;
public class BrlaBot {
    static String BOT_TOKEN = System.getenv("BOT_TOKEN");
    static String CHAT_ID = System.getenv("CHAT_ID");
    static double POC_MIN = Double.parseDouble(System.getenv().getOrDefault("POC_MIN","0.185"));
    static double POC_MAX = Double.parseDouble(System.getenv().getOrDefault("POC_MAX","0.195"));
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT","10000"));
        HttpServer s = HttpServer.create(new InetSocketAddress(port),0);
        s.createContext("/", e->{ try{String r="OK NUVEM ON"; e.sendResponseHeaders(200,r.length()); e.getResponseBody().write(r.getBytes()); e.getResponseBody().close();}catch(Exception ex){} });
        s.start();
        System.out.println("CLOUD ON PORT "+port+" POC "+POC_MIN+"-"+POC_MAX);
        try{ sendTelegram("✅ NUVEM ON! POC "+POC_MIN+" - "+POC_MAX+" | Fonte Uniswap | 24h"); }catch(Exception e){}
        boolean jaAlertou=false;
        while(true){ try{
            double[] d=getPrecoUniswap(); double preco=d[0]; double usdBrl=d[1];
            System.out.println("BRLA: $"+preco+" | USD/BRL: "+usdBrl);
            if(usdBrl<POC_MIN || usdBrl>POC_MAX){ if(!jaAlertou){ String dir=usdBrl>POC_MAX?"ALTA":"BAIXA"; sendTelegram("🚨 ROMPIMENTO "+dir+"! BRLA $"+String.format("%.4f",preco)+" | USD/BRL R$"+String.format("%.4f",usdBrl)+" saiu do POC ["+POC_MIN+" - "+POC_MAX+"]"); jaAlertou=true; } } else { jaAlertou=false; }
        }catch(Exception e){ System.out.println(e.getMessage()); } Thread.sleep(20000); }
    }
    static double[] getPrecoUniswap() throws Exception {
        String json=httpGet("https://api.dexscreener.com/latest/dex/tokens/0xe6a537a407488807f0bbeb0038b79004f19dddfb");
        int i=json.indexOf("\"priceUsd\""); int j=json.indexOf(":",i)+1; int k=json.indexOf(",",j);
        double preco=Double.parseDouble(json.substring(j,k).replaceAll("[\"}]","").trim());
        return new double[]{preco,1.0/preco};
    }
    static void sendTelegram(String t) throws Exception { httpGet("https://api.telegram.org/bot"+BOT_TOKEN+"/sendMessage?chat_id="+CHAT_ID+"&text="+URLEncoder.encode(t,"UTF-8")); }
    static String httpGet(String u) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection(); c.setRequestProperty("User-Agent","Mozilla/5.0");
        BufferedReader in=new BufferedReader(new InputStreamReader(c.getInputStream())); StringBuilder sb=new StringBuilder(); String l; while((l=in.readLine())!=null) sb.append(l); in.close(); return sb.toString();
    }
}
