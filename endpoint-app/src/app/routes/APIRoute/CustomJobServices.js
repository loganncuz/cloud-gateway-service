import {utilities} from '../../utilities';
import {ACCESS_TOKEN, PROXY_URL} from '../../config';
import * as CircularJSON from 'circular-json';




// Config URL Server Service -> must declare for each file Route Service
const proxyUrl = PROXY_URL;
const localProxy="https://localhost:6010";
const targetUrl ="/uim.document/api";

export const API_BASE_URL=proxyUrl+targetUrl;

export function downloadFile(fileName) {
 	 //console.log(" uploadFile : ",data.get('file'),proxyUrl+targetUrl,data)
	const option={
			 url: proxyUrl+targetUrl+ "/download-files/"+fileName,
		     method: 'GET',
		     responseType: 'blob'
        }
	return utilities.APIHelper.setRequest(option);
}

export function uploadFile(data,isForced) {
 	 //console.log(" uploadFile : ",data.get('file'),proxyUrl+targetUrl,data)
	const option={
			 url: proxyUrl+targetUrl+ "/upload-files/"+isForced,
		     method: 'POST',
		     body: data,
        }
	return utilities.APIHelper.setRequest(option);
}
export function removeFile(data) {
 	 //console.log(" removeFile : ",data,proxyUrl+targetUrl)
	const option={
			 url: proxyUrl+targetUrl+ "/delete-files",
		     method: 'DELETE',
		     body: JSON.stringify(data)
        }
	return utilities.APIHelper.setRequest(option);
}
export function deleteDocument(data) {
 	 console.log(" deleteDocument : ",proxyUrl+targetUrl)
	const option={
			 url: proxyUrl+targetUrl+ "/delete-document",
		     method: 'DELETE',
		     body: JSON.stringify(data)
        }
	return utilities.APIHelper.setRequest(option);
}

export function moveDocument(data) {
//	 console.log(" signup : ",proxyUrl+targetUrl)
	const option={
			 url: proxyUrl+targetUrl+ "/move-document",
		     method: 'POST',
		     body: JSON.stringify(data)
        }
	return utilities.APIHelper.setRequest(option);
}

export function updateDocument(loginRequest) {
//	 console.log(" signup : ",proxyUrl+targetUrl)
	const option={
			 url: proxyUrl+targetUrl+ "/update-document",
		     method: 'POST',
		     body: JSON.stringify(loginRequest)
        }
	return utilities.APIHelper.setRequest(option);
}

 export function getDocumentTree(offline) {
     if(!localStorage.getItem(ACCESS_TOKEN)) {
         return Promise.reject("No access token set.");
     }
//     console.log("API_BASE_URL : "+API_BASE_URL)
     const option={
     		url: API_BASE_URL+ "/document-taxonomy" ,
             method: 'GET',
        }
     return utilities.APIHelper.setRequest(option,offline);
 }