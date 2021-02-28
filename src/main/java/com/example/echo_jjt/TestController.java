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

    @RequestMapping(value = "/readtime/{seconds}")
    String sendEcho1(HttpServletRequest request,
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
        logger.info("2. Echo Data : " + dataOpt.orElseGet(() -> ""));
        logger.info("3. Timeout Seconds Data : " +
                (timeoutOpt.orElseGet(() -> "0").equals("") ? "Not exists(\"\")" : timeoutOpt.orElseGet(() -> "0")));

        // Timeout 값이 넘어오지 않은 경우
        if (seconds == null) {
            resultMap.put("result", "ERROR");
            resultMap.put("msg", "타임아웃 요청 시간 데이터가 존재하지 않습니다.");
            logger.info("[Result] : Failed -> timeout data is not exists in request");

        // Timeout 값이 넘어온 경우
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
                logger.info("[Result] : Failed -> Readout time ParseException raised");
            } catch (InterruptedException e) {
                resultMap.put("result", "ERROR");
                resultMap.put("msg", "처리 오류");
                logger.info("[Result] : Failed -> Exception raised");
            }
        }

        logger.info("--------------------------------------");

        JSONObject jsonObject = new JSONObject();
        for( Map.Entry<String, String> entry : resultMap.entrySet() ) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonObject.put(key, value);
        }

        return jsonObject.toJSONString();
    }

    @RequestMapping("/readtime")
    String sendEcho2(HttpServletRequest request,
                     @RequestParam("timeout") @Nullable String seconds,
                     @RequestBody @Nullable String data) {

        // 에러 발생의 경우 담을 result
        HashMap<String, String> resultMap = new HashMap<>();
        // Echo시 담을 result
        String resultStr;
        // Timeout 데이터
        Optional<String> timeoutOpt = Optional.ofNullable(seconds);
        // echo 데이터
        Optional<String> dataOpt = Optional.ofNullable(data);


        JSONParser parser = new JSONParser();


        // 요청 로깅
        Date reqTime = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");

        logger.info("--------------------------------------");
        logger.info("[Request In] - " + dateFormat.format(reqTime));
        logger.info("1. Request IP : " + request.getRemoteAddr() );
        logger.info("2. Echo Data : " + dataOpt.orElseGet(() -> ""));
        logger.info("3. Timeout Seconds Data : " +
                (timeoutOpt.orElseGet(() -> "0").equals("") ? "Not exists(\"\")" : timeoutOpt.orElseGet(() -> "0")));


        // 요청 파라미터가 넘어 오지 않았을 경우
        if(!timeoutOpt.isPresent()) {
            resultMap.put("result", "ERROR");
            resultMap.put("msg", "파라미터 오류 - 타임아웃 시간을 입력해주세요");

            logger.info("[Result] : Failed -> timeout data is not exists in request");
            logger.info("--------------------------------------");
        }
        // 요청 파라미터가 넘어 온 경우
        else {
            try {
                // 타임아웃 파라미터가 숫자 형식이 아닌 경우
                int readTimeMillSeconds = Integer.parseInt(seconds) * 1000;

                // 최소 시간치만큼만 Sleep
                Thread.sleep(Math.min(readTimeMillSeconds, timeOutMillSeconds));

                // Timeout이 timeOutMillSeconds보다 큰 경우 Read Timeout 발생
                if (readTimeMillSeconds > timeOutMillSeconds) {
                    resultMap.put("result", "ERROR");
                    resultMap.put("msg", "Read Timeout");
                    logger.info("[Result] : Failed -> Read Timeout");
                } else {
                    // Read Timeout 미발생 시 응답
                    resultStr = dataOpt.orElseGet(() -> "");
                    logger.info("[Result] : Echo Succeed");
                    logger.info("--------------------------------------");
                    return resultStr;
                }

            } catch (NumberFormatException e) {
                resultMap.put("result", "ERROR");
                resultMap.put("msg", "정확한 시간을 입력해주세요");
                logger.info("[Result] : Failed -> Readout time ParseException raised");
            } catch (InterruptedException e) {
                resultMap.put("result", "ERROR");
                resultMap.put("msg", "처리 중 에러 발생");
                logger.info("[Result] : Failed -> InterruptedException raised");
            }
        }

        logger.info("--------------------------------------");

        JSONObject jsonObject = new JSONObject();
        for( Map.Entry<String, String> entry : resultMap.entrySet() ) {
            String key = entry.getKey();
            String value = entry.getValue();
            jsonObject.put(key, value);
        }

        return jsonObject.toJSONString();
    }
}
