import {ACCESS_TOKEN,isOnline } from '../../app/config';
import axios from 'axios';

export const requestAxios= (options,offline) => {
//    console.log("token:",offline);
    let head={
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Access-Control-Allow-Origin': "*",
        'Access-Control-Allow-Methods': 'GET,HEAD,OPTIONS,POST,PUT',
        'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept, x-client-key, x-client-token, x-client-secret, Authorization',
    }
    if(localStorage.getItem(ACCESS_TOKEN)) {

       let secret = offline ? offline['secret'] : null
        if(secret==null){
//            console.log("options.body:",offline);
            head = Object.assign({}, {'Authorization':'Bearer ' + localStorage.getItem(ACCESS_TOKEN)}, head);
        }
        else{
            head = Object.assign({}, {'secret':'Bearer ' + secret}, head);
//                console.log("token v:",head,localStorage.getItem(ACCESS_TOKEN),secret);
        }
    }else{

    }
    if(typeof(options.headers)!=='undefined'){
        //head['Content-Type']=options.headers['Content-Type'];
        //head = Object.assign({}, {'Content-Type':options.headers['Content-Type']}, head);
        //console.log("token:",head['Authorization'],head,options.headers['Content-Type']);
    }

    const validation={
        statusCode:500,
        data:options.body,
        headers:head,
//        transformResponse: [(data) => {
//                // transform the response
//            console.log('mmmmmm',data);
//            return data;
//        }],
//        {
//            'Content-Type': 'application/json',
//            'Accept': 'application/json',
//            'Access-Control-Allow-Origin': '*',
//            'Access-Control-Allow-Headers': 'Origin,X-Requested-With,Content-Type,Accept, Authorization',
//            'Authorization': 'Bearer ' + localStorage.getItem(ACCESS_TOKEN),
//        },
//        statusCode:500,
        validateStatus: (status) => {
//                console.log('validateStatus',status);
                return status == 200; // I'm always returning true, you may want to do it depending on the status received
        }
    }

    options = Object.assign({}, validation, options);
    //console.log("token:",options.headers['Authorization'],options.headers['Content-Type']);
    return axios(options.url, options)
    .then(function(response) {
        //  console.log("response:",response,response.data,typeof(response.data));

                return response;
    })
    .catch(function(error) {
//       console.log('axios',error);
       if (error.response) {
        /*
         * The request was made and the server responded with a
         * status code that falls out of the range of 2xx
         */
//            console.log('axios error.response',error.response);
            return Promise.reject(error.response);
        } else if (error.request) {
        /*
         * The request was made but no response was received, `error.request`
         * is an instance of XMLHttpRequest in the browser and an instance
         * of http.ClientRequest in Node.js
         */
//          console.log('axios error.response',error.request);
            return Promise.reject(error.request);
        } else {
        // Something happened in setting up the request and triggered an Error
//            console.log('axios error.toJSON()',error.toJSON());
            return Promise.reject(error.toJSON());
        }

    })
    ;
};

export const requestFetch = (options,offline) => {
   const headers = new Headers({
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    })
     headers.append("Access-Control-Allow-Origin",'*');
     headers.append("Access-Control-Request-Headers", 'Origin,X-Requested-With,Content-Type,Accept, Authorization');
     headers.append("Access-Control-Allow-Headers", headers.get("Access-Control-Request-Headers"));
    
    if(localStorage.getItem(ACCESS_TOKEN)) {
//        console.log('log',headers.get('Access-Control-Allow-Origin'),headers.get('Access-Control-Allow-Headers'));
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN));
//        headers.append("Access-Control-Request-Headers", "*")
        //Access-Control-Allow-Origin,Access-Control-Allow-Headers,Access-Control-Request-Headers,
//        console.log("request ",headers,localStorage.getItem(ACCESS_TOKEN))
//        console.log("Access-Control-Allow-Origin",headers.get('Access-Control-Allow-Origin'));
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);
//    console.log("request ",options.mode,options)

    return fetch(options.url, options)
    .then(response =>
        response.json().then(json => {
//        	  console.log("API RESPONSE : ",json,options.url)
            if(!response.ok) {
            	return Promise.reject(json);
            }
//              console.log("API RESPONSE : ",json)
            return json;
        })
    ).catch(error => {
//         console.log("ERROR REQUEST:",error);

        if (isOnline){
    		//  console.log("ONLINE",options)
    		return Promise.reject(error);
    	}else{
    		// console.log("NOT ONLINE",offline)
    		return offline;
    	}
    });
}; 

export const setRequest=(options,offline)=>{
	return requestAxios(options,offline);
}

