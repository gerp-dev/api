<?php


namespace Gerp\Core;


class ApiClient
{

    private $username;
    private $password;
    private $host;

    public $verify = true;

    /**
     * Inizializza l'oggetto per l'API con le credenziali e l'host
     *
     * @param string $username il nome utente.
     * @param string $password la password.
     * @param string $host l'host al quale verranno effettuate le
     * richieste.
     */
    public function __construct($username, $password, $host)
    {
        $this->username = $username;
        $this->password = $password;

        $this->host = rtrim($host, '/') . '/';
    }



    /**
     * Effettua una richiesta di tipo GET
     *
     * @param string $url l'URL al quale effettuare la richiesta.
     */
    public function get($url, $return_value = false)
    {
        if ( $return_value ) {
            return $this->request('get', $url);
        }

        echo $this->request('get', $url);
        die;
    }



    /**
     *
     * @param $url l'URL al quale effettuare la richiesta.
     * @param array $options i parametri da passare tramite POST.
     * @param false $return_value
     * @return bool|string
     * @throws \Exception
     */
    public function post($url, $options = [], $return_value = false )
    {
        if ( $return_value ) {
            return $this->request('post', $url, $options);
        }

        echo $this->request('post', $url, $options);
        die;
    }



    private function request($type, $url, $options = [])
    {
        $type = strtolower($type);

        if ( $type !== 'get' && $type !== 'post' ) {
            throw new \Exception('Il tipo di richiesta deve essere o "get" o "post", "' . $type . '" fornito.');
        }

        $params = [];
        $url_components = parse_url($url);

        if ( ! isset($url_components['query']) ) {
            throw new \Exception('I parametri nell\'URL devono iniziare con il punto di domanda ("?"). Es.: "?manage=ricerca".');
        }

        parse_str($url_components['query'], $params);

        if ( ! isset($params['view']) || $params['view'] !== 'json' ) {
            $params['view'] = 'json';
        }

        $url = $this->host . 'index.php?' . http_build_query($params);

        $data = [
            'auth_username' => $this->username,
            'auth_secret' => $this->password
        ];

        if ( ! empty($options) ) {
            $data = array_merge($data, $options);
        }

        $curl = curl_init();
        curl_setopt($curl, CURLOPT_URL, $url);
        curl_setopt($curl, CURLOPT_TIMEOUT, 30);
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($curl, CURLOPT_POST, 1);
        curl_setopt($curl, CURLOPT_POSTFIELDS, $data);
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, $this->verify);


        if ( ! $response = curl_exec($curl) ) {
            $error_msg = "";

            if ( curl_errno($curl) ) {
                $error_msg = curl_error($curl);
            }

            throw new \Exception($error_msg . "\n" . 'Errore nella comunicazione con "' . $url . '".');
        }

        curl_close ($curl);
        return $response;
    }


}
