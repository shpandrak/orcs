import { action, observable, autorun } from 'mobx';
// import userApi from './user-api';
// import userService from './user-service';


class UserHandler {

  constructor(){

  }

}

const singleton = new UserHandler();

autorun(()=>{
  // console.log(singleton.appTemplates)
})

export default singleton;
