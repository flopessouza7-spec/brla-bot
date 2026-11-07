import java.net.*; import java.io.*; import com.sun.net.httpserver.*;
public class EuroBot {
    static String BOT_TOKEN = System.getenv("BOT_TOKEN");
    static String CHAT_ID = System.getenv("CHAT_ID");

    // --- APENAS AERODROME AGORA ---
    static String PAIR_AERODROME = "0xe846373c1a92b167b4e9cd5d8e4d6b1db9e90ec7";

    static double EURO_AERO_MIN = Double.parseDouble(System.getenv().getOrDefault("EURO_AERO_MIN","1.10516"));
    static double EURO_AERO_MAX = Double.parseDouble(System.getenv().getOrDefault("EURO_AERO_MAX","1.1274"));

    static boolean jaAlertouEuroAero = false;

    // --- CONFIRMAÇÃO DE 15 MIN ---
    static long pendenteAeroInicio = 0;
    static String pendenteAeroDir = "";
    static final long TEMPO_CONFIRMACAO = 15 * 60 * 1000; // 15 minutos

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT","10000"));
        HttpServer s = HttpServer.create(new InetSocketAddress(port),0);
        s.createContext("/", e->{ try{String r="OK NUVEM BR-EURO AERO ONLY"; e.sendResponseHeaders(200,r.length()); e.getResponseBody().write(r.getBytes()); e.getResponseBody().close();}catch(Exception ex){} });
        s.start();
        System.out.println("CLOUD ON PORT "+port+" EURO_AERO "+EURO_AERO_MIN+"-"+EURO_AERO_MAX);
        try{ sendTelegram("✅ NUVEM ON!\nEURO_AERO "+EURO_AERO_MIN+" - "+EURO_AERO_MAX+" | Fonte Aerodrome | Confirmacao 15min"); }catch(Exception e){}

        while(true){
            try{
                // --- AERODROME ---
                double precoAero = getPrecoPair(PAIR_AERODROME);
                System.out.println("EURO Aerodrome: $"+precoAero);
                if(precoAero<EURO_AERO_MIN || precoAero>EURO_AERO_MAX){
                    String dirAtual = precoAero>EURO_AERO_MAX?"ALTA":"BAIXA";
                    if(!jaAlertouEuroAero){
                        if(pendenteAeroInicio==0){
                            pendenteAeroInicio = System.currentTimeMillis();
                            pendenteAeroDir = dirAtual;
                            System.out.println("AERODROME suspeita "+dirAtual+" - aguardando 15min confirmacao");
                        } else if(!pendenteAeroDir.equals(dirAtual)){
                            pendenteAeroInicio = System.currentTimeMillis();
                            pendenteAeroDir = dirAtual;
                        } else if(System.currentTimeMillis() - pendenteAeroInicio >= TEMPO_CONFIRMACAO){
                            sendTelegram("🚨 EURO AERODROME ROMPIMENTO "+dirAtual+" CONFIRMADO! EURC $"+String.format("%.4f",precoAero)+" saiu do range ["+EURO_AERO_MIN+" - "+EURO_AERO_MAX+"] e segurou 15min fora");
                            jaAlertouEuroAero=true;
                            pendenteAeroInicio=0;
                        }
                    }
                } else { 
                    if(pendenteAeroInicio!=0) System.out.println("AERODROME voltou pra dentro - falso rompimento cancelado");
                    pendenteAeroInicio=0; pendenteAeroDir=""; jaAlertouEuroAero=false; 
                }

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
