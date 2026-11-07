import java.net.*; import java.io.*; import com.sun.net.httpserver.*;
public class BrlaBot {
    static String BOT_TOKEN = System.getenv("8912648959:AAGPmVLfcgTeXxyyfcLyN61tiejY9CxzDaI");
    static String CHAT_ID = System.getenv("7265683161");

    // --- PAIRS ---
    static String PAIR_UNISWAP = "0x7279c08A36333e12c3Fc81747963264c100D66fB";
    static String PAIR_AERODROME = "0xe846373c1a92b167b4e9cd5d8e4d6b1db9e90ec7";

    // --- EURO - você muda no Render sem mexer no código ---
    static double EURO_UNI_MIN = Double.parseDouble(System.getenv().getOrDefault("EURO_UNI_MIN","1.1029"));
    static double EURO_UNI_MAX = Double.parseDouble(System.getenv().getOrDefault("EURO_UNI_MAX","1.1250"));
    static double EURO_AERO_MIN = Double.parseDouble(System.getenv().getOrDefault("EURO_AERO_MIN","1.1000"));
    static double EURO_AERO_MAX = Double.parseDouble(System.getenv().getOrDefault("EURO_AERO_MAX","1.1300"));

    static boolean jaAlertouEuroUni = false;
    static boolean jaAlertouEuroAero = false;

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT","10000"));
        HttpServer s = HttpServer.create(new InetSocketAddress(port),0);
        s.createContext("/", e->{ try{String r="OK NUVEM BR-EURO"; e.sendResponseHeaders(200,r.length()); e.getResponseBody().write(r.getBytes()); e.getResponseBody().close();}catch(Exception ex){} });
        s.start();
        System.out.println("CLOUD ON PORT "+port+" EURO_UNI "+EURO_UNI_MIN+"-"+EURO_UNI_MAX+" EURO_AERO "+EURO_AERO_MIN+"-"+EURO_AERO_MAX);
        try{ sendTelegram("✅ NUVEM ON!\nEURO_UNI "+EURO_UNI_MIN+" - "+EURO_UNI_MAX+"\nEURO_AERO "+EURO_AERO_MIN+" - "+EURO_AERO_MAX+" | Fonte Dexscreener | 24h"); }catch(Exception e){}

        while(true){
            try{
                // --- UNISWAP ---
                double precoUni = getPrecoPair(PAIR_UNISWAP);
                System.out.println("EURO Uniswap: $"+precoUni);
                if(precoUni<EURO_UNI_MIN || precoUni>EURO_UNI_MAX){
                    if(!jaAlertouEuroUni){
                        String dir=precoUni>EURO_UNI_MAX?"ALTA":"BAIXA";
                        sendTelegram("🚨 EURO UNISWAP ROMPIMENTO "+dir+"! EURC $"+String.format("%.4f",precoUni)+" saiu do range ["+EURO_UNI_MIN+" - "+EURO_UNI_MAX+"]");
                        jaAlertouEuroUni=true;
                    }
                } else { jaAlertouEuroUni=false; }

                // --- AERODROME ---
                double precoAero = getPrecoPair(PAIR_AERODROME);
                System.out.println("EURO Aerodrome: $"+precoAero);
                if(precoAero<EURO_AERO_MIN || precoAero>EURO_AERO_MAX){
                    if(!jaAlertouEuroAero){
                        String dir=precoAero>EURO_AERO_MAX?"ALTA":"BAIXA";
                        sendTelegram("🚨 EURO AERODROME ROMPIMENTO "+dir+"! EURC $"+String.format("%.4f",precoAero)+" saiu do range ["+EURO_AERO_MIN+" - "+EURO_AERO_MAX+"]");
                        jaAlertouEuroAero=true;
                    }
                } else { jaAlertouEuroAero=false; }

            }catch(Exception e){ System.out.println(e.getMessage()); }
            Thread.sleep(20000);
        }
    }

    static double getPrecoPair(String pairAddress) throws Exception {
        String json=httpGet("https://api.dexscreener.com/latest/dex/pairs/base/"+pairAddress);
        int i=json.indexOf("\"priceUsd\""); int j=json.indexOf(":",i)+1; int k=json.indexOf(",",j);
        return Double.parseDouble(json.substring(j,k).replaceAll("[\"}]","").trim());
    }

    static String httpGet(String url) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setRequestProperty("User-Agent","Mozilla/5.0");
        BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder sb=new StringBuilder(); String l; while((l=br.readLine())!=null) sb.append(l);
        return sb.toString();
    }

    static void sendTelegram(String msg) throws Exception {
        String url="https://api.telegram.org/bot"+BOT_TOKEN+"/sendMessage?chat_id="+CHAT_ID+"&text="+URLEncoder.encode(msg,"UTF-8");
        httpGet(url);
    }
}
