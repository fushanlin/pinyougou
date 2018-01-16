//服务层
app.service('userService',function($http){
	//发送验证码
	this.sendCode=function(phone){
		return $http.get("../user/sendCode.do?phone="+phone);
	}

	//增加 
	this.add=function(entity,smscode){
		return  $http.post('../user/add.do?smscode='+smscode ,entity );
	}
});