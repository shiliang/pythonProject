package com.chainmaker.jobservice.core.parser;

/**
 * @author gaokang
 * @date 2022-08-14 16:01
 * @description:antlr4语法错误处理
 * @version: 1.0.0
 */

import com.chainmaker.jobservice.api.response.ParserException;
import org.antlr.v4.runtime.*;

public class ErrorListener extends BaseErrorListener {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        throw  new ParserException("SQL语句错误：" + "第 " + line + " 行 " + charPositionInLine +  " " + "'" + e.getOffendingToken().getText() + "'");
    }
}
