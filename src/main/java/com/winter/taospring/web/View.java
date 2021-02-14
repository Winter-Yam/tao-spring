package com.winter.taospring.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * View对象，负责解析Model，并返回页面
 */
public class View {

    public final String DEFULAT_CONTENT_TYPE = "text/html;charset=utf-8";
    // 视图代表的页面（文件）
    private File viewFile;

    public View(File viewFile) {
        this.viewFile = viewFile;
    }

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception{

        StringBuilder sb = new StringBuilder();

        // 通过 RandomAccessFile 将要返回的页面读进内存
        // 注：RandomAccessFile（提供文件读写功能） > FileInputStream + FileOutputStream。所以要指定 r或w 模式，另外 RandomAccessFile 还提供随机读写（seek等）。
        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        // 逐行读取html文件，并进行数据解析
        String line = null;
        while(null != (line = ra.readLine())) {
            // 为了字符集匹配，这里通过字节读取line，然后再new String
            line = new String(line.getBytes("ISO-8859-1"), "utf-8");

            // 通过正则表达式判断有 ￥{  } 的位置，即需要放入数据的位置
            Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            // 不断寻找有 ￥{  } 的位置
            while (matcher.find()){
                String paramName = matcher.group();
                // 获取模板中的参数名
                paramName = paramName.replaceAll("￥\\{|\\}","");
                // 在model中通过参数名获取相应参数
                Object paramValue = model.get(paramName);
                // 为 null 的话，就不管，最后输出的结果还是 ￥{ }
                if(null == paramValue){ continue;}
                // 不为null，就将 ￥{ } 替换为相应参数
                // 注意：这里对特殊字符要处理，比如异常
                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                // 更新 matcher，开始下一轮寻找
                matcher = pattern.matcher(line);
            }
            // 将当前line添加到要输出的html中
            sb.append(line);
        }

        // 将页面返回
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(sb + "");
    }

    public static String makeStringForRegExp(String str) {
        return str.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }
}
