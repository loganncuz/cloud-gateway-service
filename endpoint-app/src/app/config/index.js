import { isPlatform } from '@ionic/react';
export const ACCESS_TOKEN = 'access_token';
export const isOnline = true;
export const isDev = true;
export const isProd = true;
export const isAndroid=isPlatform('android');
export const CONTEXT_PATH=()=>{
   // console.log("CONTEXT_PATH",isAndroid);
	if(!isDev)
	 return '/'
	 else{
		 if(!isProd)
			 return ''
			 else{
			    if(isAndroid){
			         return '/'
			    }else{
//			        console.log("CONTEXT_PATH","/uim.document/");
			         return '/'
			    }
			 }

	 }
		 
}

export const MINUTES=1000 * 60;
export const HOURS=1000 * 60 * 60;
export const SECONDS=1000 ;
export const EXPIRED=30;
export const TODAY = new Date()
export const PROXY_URL = "https://10.59.67.1:8090"
//export const PROXY_URL = "https://localhost:8090"