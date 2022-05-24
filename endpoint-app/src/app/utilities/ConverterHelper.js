
 import * as CircularJSON from 'circular-json'
 
 export function ConvertToJson(source){
 	//  console.log("convertToJson :",source,CircularJSON.stringify(source))
    localStorage.setItem('cache_callback',CircularJSON.stringify(source))
 }
 
 export const ConvertToObject = (source) =>{ 
    // console.log("convertToObject :",CircularJSON.parse(source))
     return CircularJSON.parse(source);
 }