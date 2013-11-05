<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    <title>词汇维护</title>
    <script language="javascript">
    function dosubmit(url){
    	var form = document.getElementById('form1');
    	form.action=url;
    	form.submit();
    }
    </script>
  </head>  
  <body>
    <form method="get" id="form1">
    	<textarea name="wd" rows="3" cols="70"></textarea><br><br>
    	<select name="dic">
    		<option value="EXT">主词库</option>
    		<option value="STOP">停止词</option>
    	</select><br><br>
    <input type="button" value="增加" onclick="dosubmit('add.wd')">
    <input type="button" value="删除" onclick="dosubmit('del.wd')"><br><br>
    <input type="button" value="初始化" onclick="dosubmit('init.wd')">
    </form>
  </body>
</html>
