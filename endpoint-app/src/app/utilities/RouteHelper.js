 import {CONTEXT_PATH } from '../config';
 import React from 'react';
 import * as AntD from 'antd';
 import { HomeTwoTone } from '@ant-design/icons';
 import {
	    Link,  
	  } from 'react-router-dom';



export const statusMessage=(status,message,style)=>{
    let title='Control Panel - Document Services';
	if(status === 200) {
//        console.log("Success :",status,title);
        return(AntD.notification.success({
             message: title,
             description: message,
             duration: 5,
             style: {background: style,
             fontWeight:'bold'}
         }));
     } else
	if(status === 401) {
//        console.log("Error 401 :"+'Your Username or Password is incorrect. Please try again !');
        return(AntD.notification.error({
             message: title,
             description: message,
             duration: 5,
             style: {background: style,
             fontWeight:'bold'}
         }));
     } else
     if(status === 400) {
//        if(message==='') message='Bad Request';
        return(AntD.notification.error({
                     message: title,
                     description: message,
                     duration: 5,
                     style: {background: style,
                     fontWeight:'bold'}
                 }));
     }
     else {
//         console.log("Error another :"+status);
         let desc=(message!=='')?message:'500 Internal Server Error. Please contact an administrator for more support';
         return(AntD.notification.error({
             message: title,
             description: desc,
             duration: 5,
             style: {background: style,
             fontWeight:'bold'}
         }));
     }
}