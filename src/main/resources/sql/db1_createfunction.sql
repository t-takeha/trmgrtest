--
-- ストアードファンクション for Oracle
--
-- テーブル「MYTBL」から最大のnumを取得
-- 引数IN1をキーにMYTBLにレコードを登録。numは最大値+1にする
-- トランザクションはcommitもrollbackもしません。外側で何とかして。
--
create or replace function MYFUNCTEST
(
	IN1 IN VARCHAR2,    -- idの値
	OUT1 OUT NUMBER,    -- numの最大値
	OUT2 OUT VARCHAR2   -- INSERT結果ログ or SQLエラーメッセージ
)
RETURN NUMBER IS
	rc NUMBER;  -- 戻り値(0:正常、250:失敗)

	MAXNUM NUMBER(3);
BEGIN
	rc := 0;
	-- numの最大数を取得する
	SELECT MAX(num) INTO MAXNUM FROM MYTBL;
    IF (MAXNUM IS NULL) THEN
        MAXNUM := 0;
    END IF;
    OUT1 := MAXNUM;

	-- MYTBLにレコードを登録する
	INSERT INTO MYTBL (id, name, num) values (IN1, 'from MYFUNCTEST', MAXNUM+1);
	OUT2 := 'INSERT MYTBL (' || IN1 || ',from MYFUNCTEST,' || to_char(MAXNUM+1) || ')';

	RETURN rc;

EXCEPTION
	WHEN OTHERS THEN
		OUT2 := SQLERRM(SQLCODE);
		DBMS_OUTPUT.put_line(OUT2);
		rc := 250;
	RETURN rc;

END MYFUNCTEST;
/
