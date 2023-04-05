// Per richieste di tipo GET:
// api.Get("?manage=ricerca&sub=findUsers");

// Per richieste di tipo POST:
// api.Post("?manage=ricerca&sub=findUsers", new Dictionary<string, string> { { "term", "gerp" } });

public class GerpApi
{
    private string username;
    private string password;
    private string host;

    /**
     * Inizializza l'oggetto per l'API con le credenziali e l'host
     * 
     * @param string $username il nome utente.
     * @param string $password la password.
     * @param string $host l'host al quale verranno effettuate le
     * richieste.
     */
    public GerpApi(string username, string password, string host)
    {
        this.username = username;
        this.password = password;

        this.host = host.TrimEnd('/') + '/';
    }

    /**
     * Effettua una richiesta di tipo GET
     * 
     * @param string $url l'URL al quale effettuare la richiesta.
     */
    public void Get(string url)
    {
        Console.WriteLine(Request("get", url));
        Environment.Exit(0);
    }

    /**
     * Effettua una richiesta di tipo POST
     * 
     * @param string $url l'URL al quale effettuare la richiesta.
     * @param Dictionary<string, string> $options i parametri da passare tramite POST.
     */
    public void Post(string url, Dictionary<string, string> options = null)
    {
        Console.WriteLine(Request("post", url, options));
        Environment.Exit(0);
    }

    private string Request(string type, string url, Dictionary<string, string> options = null)
    {
        type = type.ToLower();

        if (type != "get" && type != "post")
        {
            throw new Exception("Il tipo di richiesta deve essere o \"get\" o \"post\", \"" + type + "\" fornito.");
        }

        var paramsDict = new Dictionary<string, string>();
        var urlComponents = new Uri(url);

        if (string.IsNullOrEmpty(urlComponents.Query))
        {
            throw new Exception("I parametri nell'URL devono iniziare con il punto di domanda (\"?\"). Es.: \"?manage=ricerca\".");
        }

        var queryString = urlComponents.Query.Substring(1);
        foreach (var param in queryString.Split('&'))
        {
            var keyValue = param.Split('=');
            paramsDict.Add(keyValue[0], keyValue[1]);
        }

        if (!paramsDict.ContainsKey("view") || paramsDict["view"] != "json")
        {
            paramsDict["view"] = "json";
        }

        url = this.host + "index.php?" + string.Join("&", paramsDict.Select(x => x.Key + "=" + x.Value));

        var data = new Dictionary<string, string>
        {
            { "auth_username", this.username },
            { "auth_secret", this.password }
        };

        if (options != null)
        {
            foreach (var option in options)
            {
                data.Add(option.Key, option.Value);
            }
        }

        using (var client = new WebClient())
        {
            var responseBytes = client.UploadValues(url, "POST", new NameValueCollection(data));
            var response = Encoding.UTF8.GetString(responseBytes);
            return response;
        }

        throw new Exception("Errore nella comunicazione con \"" + url + "\".");
    }
}
