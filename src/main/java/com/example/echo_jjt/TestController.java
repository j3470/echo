package com.example.echo_jjt;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@ResponseBody
@RequestMapping("/echo")
public class TestController {

    private final int timeOutMillSeconds = 30000;
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping(value = {"/readtime/{seconds}", "/readtime"})
    String sendEcho(HttpServletRequest request,
                    @RequestBody @Nullable String data,
                    @PathVariable("seconds") @Nullable String seconds) {

        // Echo 시 담을 result
        String resultStr = "";
        // 에러 발생의 경우 담을 result
        HashMap<String, String> resultMap = new HashMap<>();
        // Body 데이터
        Optional<String> dataOpt = Optional.ofNullable(data);
        // Timeout 데이터
        Optional<String> timeoutOpt = Optional.ofNullable(seconds);

        JSONParser parser = new JSONParser();

        // 요청 로깅
        Date reqTime = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");

        logger.info("--------------------------------------");
        logger.info("[Request In] - " + dateFormat.format(reqTime));
        logger.info("1. Request IP : " + request.getRemoteAddr() );
        logger.info("2. Echo DATA : " + dataOpt.orElseGet(() -> ""));
        logger.info("3. Timeout Seconds : " + timeoutOpt.orElseGet(() -> "0"));

        // Timeout이 넘어오지 않은 경우 ( /readtime )
        if (seconds == null) {
            resultStr = dataOpt.orElseGet(() -> "");
            logger.info("[Result] : Echo Succeed");
            logger.info("--------------------------------------");
            return resultStr;

        // Timeout이 넘어온 경우 ( /readtime/{seconds} )
        } else {
            try {
                int readTimeMillSeconds = Integer.parseInt(seconds) * 1000;

                // 최소 시간치만큼만 Sleep
                Thread.sleep(Math.min(readTimeMillSeconds, timeOutMillSeconds));

                // Timeout이 timeOutMillSeconds보다 큰 경우 Read Timeout 발생
                if ((readTimeMillSeconds - timeOutMillSeconds) >= 0) {
                    resultMap.put("result", "ERROR");
                    resultMap.put("msg", "Read Timeout");
                    logger.info("[Result] : Failed -> Read Timeout");

                // Read Timeout 미발생 시 응답
                } else {
                    resultStr = dataOpt.orElseGet(() -> "");
                    logger.info("[Result] : Echo Succeed");
                    logger.info("--------------------------------------");
                    return resultStr;
                }
            }  catch (NumberFormatException e) {
                resultMap.put("result", "ERROR");
                resultMap.put("msg", "정확한 시간을 입력해주세요");
                logger.info("[Result] : Failed -> Readout time ParseException throwed");
            } catch (InterruptedException e) {
                resultMap.put("result", "ERROR");
                resultMap.put("msg", "처리 오류");
                logger.info("[Result] : Failed -> Exception throwed");
            }
        }

        logger.info("--------------------------------------");

        JSONObject jsonObject = new JSONObject();
        for( Map.Entry<String, String> entry : resultMap.entrySet() ) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }

        return jsonObject.toJSONString();
    }
}
