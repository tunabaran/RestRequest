# RestRequest

Android easy RestRequest library.

RestRequest is a wrapper library that uses android standart HttpRequest components to make JSON Rest Calls. RestRequest is aimed on achieving easy implementation, reuseable request objects, instinctive configuration options. 

You can use RestRequest to create fast request on anywhere in code base or define requests of your application, configure accordingly and reuse.

## INSTALLATION

1-) Add repository to Project level gradle file.
```

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "http://restrequest.s3.amazonaws.com" }
    }
}

```

2-)Add dependency to App level gradle file.
```

implementation (group: 'com.tunabaranurut', name: 'restrequest', version: '0.0.1', ext: 'aar', classifier: 'debug'){
    transitive = true
}

```

## EASY USE EXAMPLE

You can create a simple request with these lines of code;

```

RestRequest restRequest = new RestRequest(url);
restRequest.setOnRequestSuccessCallback(new OnRequestSuccessCallback() {
    @Override
    public void onSuccess(ApiResponse apiResponse) {
        Response response = (Response) apiResponse.getData();
        // Handle callback here.
    }
});
restRequest.exchange(request, RequestType.POST, Response.class);

```

## GETTING STARTED

RestRequest library is based on RestRequest class. In order to make a request you need to create an object instance of RestRequest and provide an url. Then to perform request, call exchange method which requires; request object, Request type, and Response Class.

RestRequest class transforms request object to json, makes request and takes response json, transforms it to Response Class instance then returns in OnRequestSuccessCallback method. If you expect a response payload then you must use 
setOnRequestSuccessCallback() method and catch request success event.

### Constructor

RestRequest has two constructors available. One of them is;

```

public RestRequest(String requestUrl);


```

**requestUrl** is a required parameter to make http requests. You need to provide an url including "http://" or "https://" part of it.

Second constructor is;

```

public RestRequest(String requestUrl, RestRequestCallback callback);


```
RestRequestCallback parameter is primary way to handle request callbacks. 

```

  void onSuccess(ApiResponse var1);

  void onFailed();

  void onRetry(int var1);

  void onCancel();

```

### Handling Callbacks

**RestRequestCallback**

RestRequest has a constructor that takes RestRequestCallback as parameter. This interface contains four methods;

**onSuccess** method is called when request is successfully completed. Provides ApiResponse object as method parameter. ApiResponse object contains getData method to provide response payload. Which may be null depends on target source.

**onFailed** method is called when request is failed. 

**onRetry** method is called when request is retrying. Contains an integer variable corresponds to remaining retry count. RestRequest class can make requests after failed. This option will be explained later.

**onCancel** method is called when RestRequestTask is cancelled. This event may be called by AsyncTaskQueue. This will be explained later.

```

 RestRequest restRequest = new RestRequest(url, new RestRequestCallback() {
        @Override
        public void onSuccess(ApiResponse apiResponse) {
            
        }

        @Override
        public void onFailed() {

        }

        @Override
        public void onRetry(int i) {

        }

        @Override
        public void onCancel() {

        }
    });

```

If you dont want to give all 4 callback methods directly on constructor. You can also use seperated methods to register callbacks.

These callback interface method parameters are same as RestRequestCallback method parameters.

If you use RestRequestCallback on constructor and register seperated listeners using these methods. Both callback methods will be called. RestRequestCallback listeners are called first.

```

restRequest.setOnRequestSuccessCallback(new OnRequestSuccessCallback() {
    @Override
    public void onSuccess(ApiResponse apiResponse) {

    }
});
        
restRequest.setOnRequestFailedCallback(new OnRequestFailedCallback() {
    @Override
    public void onFailed() {

    }
});

restRequest.setOnRequestRetryCallback(new OnRequestRetryCallback() {
    @Override
    public void onRetry(int i) {

    }
});

restRequest.setOnRequestCancelCallback(new OnRequestCancelCallback() {
    @Override
    public void onCancel() {

    }
});

```

## EXCHANGE

Exchange is the key method of RestRequest class. Takes three parameters RequestType, RequestPayload and Response Class.

You can call exchange method more than once.

**RequestType :** Supports GET and POST requests. You can Select using **RequestType.GET** or **RequestType.POST**.

**RequestPayload:** This object parameter can take any object Jackson can transform to json. This parameter can be null.

**ResponseClass :** This parameters is used to parse response json to Object. Object will be instance of this class. You can access reponse object from ApiResponse object when onRequestSuccess callback called.

Use examples;

```
//POST request
restRequest.exchange(request, RequestType.POST, Response.class);

//GET request
restRequest.exchange(null, RequestType.GET, Response.class);

```

## ApiResponse

RestRequest class returns ApiResponse object at OnRequestSuccessCallback onSuccess method.

ApiResponse contains two properties.

**Object data :** This property contains response payload object. This may be null.
**ResponseStatus :** This property show response current state. May be RAW, READY, SENT.


## CONFIGURATION

### Retrying After Failed

RestRequest has functionality to remake a request after failed. This feature may be usefull for applications that makes calls to unstable endpoint. You can retry count using;

```

restRequest.setRetryCount(3);

```

You can also change default retry count using GlobalRestConfig class using;

```

GlobalRestConfig.retryCount = 3;

```

### Sync And Async Usage

RestRequest uses Android AsyncTask class to perform http calls in a different thread. AsyncTask class works with PoolExecutors which has two options. SERIAL_EXECUTOR and THREAD_POOL_EXECUTOR. RestRequest uses these executors to make calls sync and async. For more importation check AsyncTask documentation. 

By default RestRequest makes sync calls. You can change this behavior using RestRequest object for individual requests. Or you can set static GlobalRestConfig.async property to change default behaviour.

```

restRequest.setAsync(true);

```

```

GlobalRestConfig.async = true;

```

### Authentication

RestRequest supports direct basic authentication. You can set it easly using;

```

restRequest.setbasicAuth(username,password);

```

If your endpoint requires other Authentication methods you can achieve it using RequestHeader.

### Request Headers

RestRequest uses RequestHeader class to manage HTTP request headers. 

RequestHeader object has a single constructor that takes "name" "value" string parameters. You can create a new RequestHeader using;

```

RequestHeader requestHeader = new RequestHeader("Content-Type","application/json");

```

Add RequestHeader to RestRequest using;

```

restRequest.addHeader(requestHeader);

```

You can also create a List of RequestHeaders and set it as RestRequest headers using;

**If you set Headers using this method, it overrides all previous addHeader calls.

```

restRequest.setHeaders(headers);

```

RestRequest comes with default request headers defined in GlobalRestConfig. These headers are;

("Accept","application/json");
("Content-Type","application/json; charset=UTF-8");

You can change default headers with setting GlobalRestConfig static public static List<RequestHeader> requestHeaders method using;

```

GlobalRestConfig.requestHeaders = new LinkedList<>(Arrays.asList(
        new RequestHeader("Accept","application/json"),
        new RequestHeader("Content-Type","application/json; charset=UTF-8")
));

```

Headers defined in GlobalRestConfig are added after RestConfig headers list. If you set headers using both method request will contain both headers.

### INTERCEPTORS

RestRequest uses Jackson library to change request and response objects to json. You can access and change request and response json Strings using PreExecuteInterceptor and PostExecuteInterceptor interfaces.

**PreExecuteInterceptor**

You can register this Interceptor to get request json. PreExecuteInterceptor contains String onPreExecute(String s) method. Upon registering this method is called on RestRequest execute method just before making http calls. This method provides request json as String s parameter.

**You need return request string as returning parameter of onPreExecute.

You can register PreExecuteInterceptor using;

```

restRequest.setPreExecuteInterceptor(new PreExecuteInterceptor() {
    @Override
    public String onPreExecute(String s) {
        return s;
    }
});

```

You can also register Global PreExecuteInterceptor with GlobalRestConfig class.

If you register listeners to both RestRequest and GlobalRestConfig, they both get called but GlobalRestConfig interceptor is called first.

You can register Global PreExecuteInterceptor using;

```

GlobalRestConfig.preExecuteInterceptor = new PreExecuteInterceptor() {
    @Override
    public String onPreExecute(String s) {
        return s;
    }
};

```

**PostExecuteInterceptor**

You can register this Interceptor to get response json. PostExecuteInterceptor contains String onPostExecute(String s) method. Upon registering this method is called on RestRequest execute method just before making http calls. This method provides response json as String s parameter. 

**You need return response string as returning parameter of onPostExecute.

You can register PostExecuteInterceptor using;

```

restRequest.setPostExecuteInterceptor(new PostExecuteInterceptor() {
    @Override
    public String onPostExecute(String s) {
        return s;
    }
});

```

You can also register Global PostExecuteInterceptor with GlobalRestConfig class.

If you register listeners to both RestRequest and GlobalRestConfig, they both get called but GlobalRestConfig interceptor is called first.

You can register Global PostExecuteInterceptor using;

```

GlobalRestConfig.postExecuteInterceptor = new PostExecuteInterceptor() {
    @Override
    public String onPostExecute(String s) {
        return s;
    }
};

```

## GLOBAL CONFIGURATION

GlobalConfiguration class is an abstract class with static properties. These properties are used default/base properties for RestRequest class. 

All properties are public so you can change any property using class.

Default global properties are;

```

public static int retryCount = 0;
public static boolean async = false;
public static AuthorizationHeader authorizationHeader = null;
public static boolean addTasksToQueue = false;

public static List<RequestHeader> requestHeaders = new LinkedList<>(Arrays.asList(
        new RequestHeader("Accept","application/json"),
        new RequestHeader("Content-Type","application/json; charset=UTF-8")
));

public static boolean DEBUG = false;

public static OnRequestSuccessCallback onRequestSuccessCallback = null;
public static OnRequestRetryCallback onRequestRetryCallback = null;
public static OnRequestCancelCallback onRequestCancelCallback = null;
public static OnRequestFailedCallback onRequestFailedCallback = null;

public static PreExecuteInterceptor preExecuteInterceptor = null;
public static PostExecuteInterceptor postExecuteInterceptor = null;

```

### CONTACT AND CONTRIBUTING

You fork this repository and contribute back using pull requests.

Any contributions, features, bug fixes, are welcomed and appreciated but will be reviewed and discussed.

If you have question or need help send an email at "tunaurut@gmail.com"
