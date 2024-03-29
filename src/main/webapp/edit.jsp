<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>     
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="./css/style.css" /> 
</head>
<body>
	<div class="board_wrap">
		<div class="board_title">
			<strong>자유게시판</strong>
			<p>자유게시판 입니다.</p>
		</div>
		<div class="board_write_wrap">
			<!-- 파일 업로드르 위해선 form 태그안에 enctype="multipart/form-data"가 반드시 필요하다. -->
			<form name="frm" method="post" action="update?board_no=${board.board_no}" enctype="multipart/form-data">
			<div class="board_write">
				<div class="title">
					<dl>
						<dt>제목</dt>
						<dd><input type="text" name="title" maxlength="50" value="${board.title}" /></dd>
					</dl>
				</div>
				<div class="info">
					<dl>
						<dt>글쓴이</dt>
						<dd><input type="text" name="user_id" maxlength="10" value="${board.user_id}" /></dd>
					</dl>
				</div>
				<div class="cont">
					<textarea name="content">${board.content}</textarea>
				</div>
				<div style="padding-top:10px;">
					<label style="font-size: 1.4rem; padding-right: 20px;" for="file">이미지 업로드</label>
					<input type="file" name="file" id="file" />
					<br />
					<c:if test="${board.img != null}">
						<img alt="업로드 이미지" src="${board.img}" width="100" />
					</c:if>
					<input type="hidden" name="origin_file" value="${board.img}" />
					<!-- 수정을 눌렀을때 등록된 이미지가 없을 경우 value는 없다. 등록된 이미지가 있을 경우 value는 이미지 경로를 가지고 온다. <- 서버단에서 필요 -->
					<!-- 수정시 이미지 선택을 안하면 이미지가 없어지는 현상을 방지 -->
				</div>
			</div>
			</form>
			<div class="bt_wrap">
				<a onclick="chkForm(); return false;" class="on">수정</a>
				<a href="index">취소</a>
			</div>
		</div>
	</div>
	<script type="text/javascript" src="./js/script.js"></script>
</body>
</html>