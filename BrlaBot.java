import java.net.*; import java.io.*; import com.sun.net.httpserver.*;
public class BrlaBot {
    static String BOT_TOKEN = System.getenv("BOT_TOKEN");
    static String CHAT_ID = System.getenv("CHAT_ID");
    // BRLA
    static double POC_MIN = Double.parseDouble(System.getenv().getOrDefault("POC_MIN","0.185"));
    static double POC_MAX = Double.parseDouble(System.getenv().getOrDefault("POC_MAX","0.195"));
    // EURO - NOVO - você muda no Render sem mexer no código
    static double EURO_MIN = Double.parseDouble(System.getenv().getOrDefault("EURO_MIN","1.10"));
    static double EURO_MAX = Double.parseDouble(System.getenv().getOrDefault("EURO_MAX","1.12"));

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT","10000"));
        HttpServer s = HttpServer.create(new InetSocketAddress(port),0);
        s.createContext("/", e->{ try{String r="OK NUVEM ON BRLA+EURO"; e.sendResponseHeaders(200,r.length()); e.getResponseBody().write(r.getBytes()); e.getResponseBody().close();}catch(Exception ex){} });
        s.start();
        System.out.println("CLOUD ON PORT "+port+" BRLA POC "+POC_MIN+"-"+POC_MAX+" EURO "+EURO_MIN+"-"+EURO_MAX);
        try{ sendTelegram("✅ NUVEM ON!\nBRLA POC "+POC_MIN+" - "+POC_MAX+"\nEURO "+EURO_MIN+" - "+EURO_MAX+" | Fonte Dexscreener | 24h"); }catch(Exception e){}
        
        boolean jaAlertouBrla=false;
        boolean jaAlertouEuro=false;
        
        while(true){ try{
            // --- BRLA ---
            double[] d=getPrecoBRLA(); double precoBRLA=d[0]; double usdBrl=d[1];
            System.out.println("BRLA: $"+precoBRLA+" | USD/BRL: "+usdBrl);
            if(usdBrl<POC_MIN || usdBrl>POC_MAX){ if(!jaAlertouBrla){ String dir=usdBrl>POC_MAX?"ALTA":"BAIXA"; sendTelegram("🚨 BRLA ROMPIMENTO "+dir+"! BRLA $"+String.format("%.4f",precoBRLA)+" | USD/BRL R$"+String.format("%.4f",usdBrl)+" saiu do POC ["+POC_MIN+" - "+POC_MAX+"]"); jaAlertouBrla=true; } } else { jaAlertouBrla=false; }

            // --- EURO ---
            double precoEuro = getPrecoEURO(); // EUR/USD
            System.out.println("EURO: $"+precoEuro);
            if(precoEuro<EURO_MIN || precoEuro>EURO_MAX){ if(!jaAlertouEuro){ String dir=precoEuro>EURO_MAX?"ALTA":"BAIXA"; sendTelegram("🚨 EURO ROMPIMENTO "+dir+"! EURC $"+String.format("%.4f",precoEuro)+" saiu do range ["+EURO_MIN+" - "+EURO_MAX+"]"); jaAlertouEuro=true; } } else { jaAlertouEuro=false; }

        }catch(Exception e){ System.out.println(e.getMessage()); } Thread.sleep(20000); }
    }
    
    static double[] getPrecoBRLA() throws Exception {
        String json=httpGet("https://api.dexscreener.com/latest/dex/tokens/0xe6a537a407488807f0bbeb0038b79004f19dddfb");
        int i=json.indexOf("\"priceUsd\""); int j=json.indexOf(":",i)+1; int k=json.indexOf(",",j);
        double preco=Double.parseDouble(json.substring(j,k).replaceAll("[\"}]","").trim());
        return new double[]{preco,1.0/preco};
    }

    static double getPrecoEURO() throws Exception {
        // EURC Base - Aerodrome
        String json=httpGet("https://api.dexscreener.com/latest/dex/tokens/0x60a3E35Cc302bFA44Cb288Bc5a4F316FDB1adb42");
        int i=json.indexOf("\"priceUsd\""); int j=json.indexOf(":",i)+1; int k=json.indexOf(",",j);
        double preco=Double.parseDouble(json.substring(j,k).replaceAll("[\"}]","").trim());
        return preco;
    }

    static void sendTelegram(String t) throws Exception { httpGet("https://api.telegram.org/bot"+BOT_TOKEN+"/sendMessage?chat_id="+CHAT_ID+"&text="+URLEncoder.encode(t,"UTF-8")); }
    static String httpGet(String u) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection(); c.setRequestProperty("User-Agent","Mozilla/5.0");
        BufferedReader in=new BufferedReader(new InputStreamReader(c.getInputStream())); StringBuilder sb=new StringBuilder(); String l; while((l=in.readLine())!=null) sb.append(l); in.close(); return sb.toString();
    }
}
