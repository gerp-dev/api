' Per richieste di tipo GET:
' api.get("?manage=ricerca&sub=findUsers")
'
' Per richieste di tipo POST:
' api.post("?manage=ricerca&sub=findUsers", New Dictionary(Of String, String) From {{"term", "gerp"}})

Public Class GerpApi
    Private username As String
    Private password As String
    Private host As String

    ''' <summary>
    ''' Inizializza l'oggetto per l'API con le credenziali e l'host
    ''' </summary>
    ''' <param name="username">il nome utente.</param>
    ''' <param name="password">la password.</param>
    ''' <param name="host">l'host al quale verranno effettuate le richieste.</param>
    Public Sub New(ByVal username As String, ByVal password As String, ByVal host As String)
        Me.username = username
        Me.password = password

        Me.host = host.TrimEnd("/"c) & "/"
    End Sub

    ''' <summary>
    ''' Effettua una richiesta di tipo GET
    ''' </summary>
    ''' <param name="url">l'URL al quale effettuare la richiesta.</param>
    Public Sub [Get](ByVal url As String)
        Console.WriteLine(Request("get", url))
        Environment.Exit(0)
    End Sub

    ''' <summary>
    ''' Effettua una richiesta di tipo POST
    ''' </summary>
    ''' <param name="url">l'URL al quale effettuare la richiesta.</param>
    ''' <param name="options">i parametri da passare tramite POST.</param>
    Public Sub Post(ByVal url As String, Optional ByVal options As Dictionary(Of String, String) = Nothing)
        Console.WriteLine(Request("post", url, options))
        Environment.Exit(0)
    End Sub

    Private Function Request(ByVal type As String, ByVal url As String, Optional ByVal options As Dictionary(Of String, String) = Nothing) As String
        type = type.ToLower()

        If type <> "get" AndAlso type <> "post" Then
            Throw New Exception("Il tipo di richiesta deve essere o ""get"" o ""post"", """ & type & """ fornito.")
        End If

        Dim params As New Dictionary(Of String, String)()
        Dim url_components As UriComponents = (New Uri(url)).GetComponents(UriComponents.Query, UriFormat.UriEscaped)

        If Not url_components.StartsWith("?") Then
            Throw New Exception("I parametri nell'URL devono iniziare con il punto di domanda (""?""). Es.: ""?manage=ricerca"".")
        End If

        url_components = url_components.Substring(1)
        Dim query_params As String() = url_components.Split("&"c)

        For Each query_param As String In query_params
            Dim param_parts As String() = query_param.Split("="c)
            params.Add(param_parts(0), param_parts(1))
        Next

        If Not params.ContainsKey("view") OrElse params("view") <> "json" Then
            params("view") = "json"
        End If

        url = Me.host & "index.php?" & String.Join("&", params.[Select](Function(p) p.Key & "=" & p.Value))

        Dim data As New Dictionary(Of String, String) From {{"auth_username", Me.username}, {"auth_secret", Me.password}}

        If options IsNot Nothing AndAlso options.Count > 0 Then
            For Each option As KeyValuePair(Of String, String) In options
                data.Add(option.Key, option.Value)
            Next
        End If

        Dim client As New System.Net.WebClient()
        Dim response As String = client.UploadString(url, "POST", String.Join("&", data.[Select](Function(d) d.Key & "=" & d.Value)))

        Return response
    End Function
End Class
