// Per richieste di tipo GET:
// api.get("?manage=ricerca&sub=findUsers");

// Per richieste di tipo POST:
// Map<String, String> options = new HashMap<>();
// options.put("term", "gerp");
// api.post("?manage=ricerca&sub=findUsers", options);

public class GerpApi {
    private String username;
    private String password;
    private String host;

    /**
     * Inizializza l'oggetto per l'API con le credenziali e l'host
     * 
     * @param username il nome utente.
     * @param password la password.
     * @param host l'host al quale verranno effettuate le richieste.
     */
    public GerpApi(String username, String password, String host) {
        this.username = username;
        this.password = password;
        this.host = host.endsWith("/") ? host : host + "/";
    }

    /**
     * Effettua una richiesta di tipo GET
     * 
     * @param url l'URL al quale effettuare la richiesta.
     */
    public void get(String url) {
        System.out.println(request("get", url));
        System.exit(0);
    }

    /**
     * Effettua una richiesta di tipo POST
     * 
     * @param url l'URL al quale effettuare la richiesta.
     * @param options i parametri da passare tramite POST.
     */
    public void post(String url, Map<String, String> options) {
        System.out.println(request("post", url, options));
        System.exit(0);
    }

    private String request(String type, String url, Map<String, String> options) {
        type = type.toLowerCase();

        if (!type.equals("get") && !type.equals("post")) {
            throw new IllegalArgumentException("Il tipo di richiesta deve essere o \"get\" o \"post\", \"" + type + "\" fornito.");
        }

        Map<String, String> params = new HashMap<>();
        String[] url_components = url.split("\\?");

        if (url_components.length < 2) {
            throw new IllegalArgumentException("I parametri nell'URL devono iniziare con il punto di domanda (\"?\"). Es.: \"?manage=ricerca\".");
        }

        String[] query_params = url_components[1].split("&");

        for (String query_param : query_params) {
            String[] param = query_param.split("=");
            params.put(param[0], param[1]);
        }

        if (!params.containsKey("view") || !params.get("view").equals("json")) {
            params.put("view", "json");
        }

        url = host + "index.php?" + params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        Map<String, String> data = new HashMap<>();
        data.put("auth_username", username);
        data.put("auth_secret", password);

        if (options != null && !options.isEmpty()) {
            data.putAll(options);
        }

        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod(type.toUpperCase());
            conn.setConnectTimeout(30000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] postDataBytes = data.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .collect(Collectors.joining("&"))
                        .getBytes("UTF-8");
                os.write(postDataBytes);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore nella comunicazione con \"" + url + "\".", e);
        }
    }
}
