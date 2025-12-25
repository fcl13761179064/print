package com.programe.print;

import static java.sql.DriverManager.println;

import android.app.IntentService;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.blankj.utilcode.util.GsonUtils;
import com.programe.print.print.GPrinterCommand;
import com.programe.print.print.PrintPic;
import com.programe.print.print.PrintQueue;
import com.programe.print.print.PrintUtil;
import com.programe.print.printutil.PrintOrderDataMaker;
import com.programe.print.printutil.PrinterWriter;
import com.programe.print.printutil.PrinterWriter58mm;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import kotlinx.coroutines.Dispatchers;

/**
 * Created by liuguirong on 8/1/17.
 * <p/>
 * print ticket service
 */
public class BtService extends IntentService {

    public BtService() {
        super("BtService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BtService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("BtService", "========= onHandleIntent 被调用 =========");
        if (intent == null || intent.getAction() == null) {
            Log.e("BtService", "intent 或 action 为空");
            return;
        }
        Log.d("BtService", "Action: " + intent.getAction());

        if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST)) {
            String data = intent.getStringExtra("PRINT_DATA");
            Log.d("BtService", "收到打印数据: " + (data != null ? data.substring(0, Math.min(100, data.length())) + "..." : "null"));
            printTest(data);
            Log.d("BtService", "正在打印中.....");
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST_TWO)) {
            printTesttwo(3);
        }else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_BITMAP)) {
            String ss ="data:image/jpeg;base64,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";
            printBitmapTest(ss);
            //printSingleSealExample(ss);
        }

    }

    private void printTest(String data) {
            Log.d("BtService", "========= printTest 被调用 =========");
            Log.d("BtService", "准备创建 PrintOrderDataMaker...");
            PrintOrderDataMaker printOrderDataMaker = new PrintOrderDataMaker(this,data, PrinterWriter58mm.TYPE_58, PrinterWriter.HEIGHT_PARTING_DEFAULT);
            Log.d("BtService", "PrintOrderDataMaker 创建完成");
            ArrayList<byte[]> printData = (ArrayList<byte[]>) printOrderDataMaker.getPrintData(PrinterWriter58mm.TYPE_58);
            Log.d("BtService", "打印数据生成完成, 数据块数量: " + (printData != null ? printData.size() : 0));
            PrintQueue.getQueue(getApplicationContext()).add(printData);
            Log.d("BtService", "打印数据已添加到队列");

           new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    String ss ="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCADwAPQDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD6pooooAKKKKACiiigAooooAKKM1nX2r21r8obzJP7q0FRhKbtFGjTJZY4lzLIiD1Y4rkda1TUZSos5I7cd1A3M30rK0HR76+vvNvY5ggYHzJGwT17Gg7oYH3HOpNRt06ndT6jbwpuL54zhRmsi/8AEy22nvdx2jyohwRvx/Sp4tGmkef7ZcqyuRgIuMAdKsx6LZJbeQ0e+L0YmmZx+rQtze9/XyOSm8dXfnIsOnRBW/vSnI/DFTf8Jjdpbs8lpHv3AYBPeukPh7SmYF7KFivQkcip/wCydP8A+fSL/vmg6JYjBactL8X/AJnPWfi5ppGjNqNy8ffx/Sr6+IikbNcWhXH92QN/SrraFpjPv+yIH/vLkH9KY/h+wYfIsiH1Dt/U0Gbq4OX2Wv69SOPxLp5CmWR4QRnLocD6noK1La7t7qMPbTRzIf4o2DD9KxpfD7BnaK7c7kKbZFBX9MVw8nhHU7C6kkgjcMwJE1u4GOM/d600k9zSlhcNWvapyvz/AKX6nrFFeYWPivV9PvxZ3pinRQAVcFHz9f8A61djo/iew1B/Kd/s91nHlSHk/Q96HGxjXy+tQXM1dd0b1FAPFGak4QooooAKKKKACiiigAooooAKKKKACiiigAooooAKo6jqdvYJmZsuRlUXkmq+q6p9mPlW4DS9yeij1rnf7IutU1IXSzMFIwWIxx7UHXQwyl71V2iZeo67rGo3vlQgwRHpFHy5/wCBV0llolxcIhvJDGo5MYwSfxra07TYLCPEK/OfvOepq6KDWvjYtKFCPKl1ILSzhtVAiQD3qxRRQcDberKOqX66fFHI0bOGcJ8vbPei9v1tbuzhZCftDlAwPQgZqPXkEmmTeqgOPwIp15Al0LSbOPKkDj8Rj+tM1jCNk2u/5aGdNcXMfjO3h3N9kmtmO3tuFdBWVqdp5upadcK2DAzA+4YYxWqKAqOLUXHtr97KGs3wsLQSDBdmCLnuSaamp7Ima6tbmEr1/d7s/TGazfFKi81HSLIdTP5zD/ZUZ/nit0S/v2ibggAj3H/66CpQjGnFtau7+X9XKB8RaUsLSvfRIijcd+VOPoea0LaeK6t45oHDxSKHVh3B6Gs9Vh1SaeO6s4pYUbaC65rRghWCJI41CooCqAMAAUiKigtEncg1DTrTUI9l5bpKv+0P61yuqeD9kYOnbXCtuCy9QfY12tZuka1ZatLdpYyiX7M4RyOmT6UzXD4ivRTlTei+45i21O800pCi/LEdrwSf+ytXU6XrFpqO5YJB5q/fjJ+ZfwqTUtNttRgMdymc9GHBFcH4m8PTafi8ieVnUgJcRcNH6ZH+RSOuKoYx8r92b+5/19/qek0VyHhnxQ0pjstb2xXjAbJAPll68ex9q6/NNqxwV6E6EuSa/wCD6BRRRSMQooooAKKKKACiiigAoooNABXO61q7ec1pajDEfNIe30qxrN5K6tBYuhdf9cM4IX296i0vSNsizSnKY+VT1P1oOyjCFNe0q/JDNK0xpEWW5DADsT96t9EVFCoMAUqjA4paDnq1ZVXdjZYxLGyNnawIOKx9Pil0554IXe6jzmNS3KH+7n0pPFU7xW1qI/MHmTqjFM52nr0q3PCLO0X7IPLVSM46nPGTTLgrRV/tfoZkdprR1yOaW9VLRhl4QuQPbPr710VV728t7NA9zIqA8DJ6n0qtost7cQPLemHa7boRGOQnbJz1oCpKU4qbSSWm1v8Ahya7YG4itzz5gbd9MVDbMVtUJ/1YiBz2yKuvCjSLIy/OowD3ArO0S4W/0bOP7yH8CRQEfgv0TX6j9Rn2W5mf5Yl2uG/pWkhyoPqKxbg/2j4VdsbS8JOPcf8A6q0NHk87TLaQ9WjUn8qBTilDzTaMqEi48W3JIz9liVR7FhmrOruyXtmYyQ5JU49DVLw/Opjv9QbBNxcEIO7BflA/Sm69E1npk+ouN138uOeF5wB+tOx1ct6qh6L521/E6G1hSCFY4hhBUkjiNGdjhVGSaZaMXtombqygn8qxNc18Wd3FYWMButQlOFjJ2qB1yxNJJvY5IU51ZWirkFzcQ6/ZOst0LbT3fy2AOGk9s9qt6U+kaZONL01I4sAswTpn3PqaS00cOrnUhFMWfeEVMKv+P1NTxnTrKEwMkNqpP3GwuT7UPQ3nONnTje3ZbevmM0Vrp77VJbkMsLzAQhh2CgE/mK1ZI1kRkcAqeCD3rAt59WTxFHbJan+xghzO5Xdnbx3z19q6PNDVjGvFqSemqW39b9zzzxv4blht5LrTw8sOQZIM9B7e1Wfh3rmoXUMltqdrOsMfEV1L8u4ZwF55J9+/8+5rzb4jaPcWjtqsEsj25I85SxPl9MEe1XFprlPUwuIWMgsHWtfpJ/kelUVw/gDxYNTxpt+3+mouY5D/AMtl/wARXcZqGrHmYnDVMNUdKotV/VwooopGAUUUUAFFFFABVLUbv7NFhf8AWNwo/rVqaRY0LOcKBk1zEMjak/2td4eT5Y17AeuKDehS53zS2ResrIPcbx9wnfIw/iPpW2BUVrCIIVRecd/WqWsaPa6oqi7DkocqVcrj8uv40CclUn77sv69C9cSiGCSRuiKTycVR0C7vL3TY57+2S3lfJCI275exqvbFVZbH961vEAN7rw3tmrup3zWUQMcDTHqVXqAO9MHTsuRK7fX+u5drPuJ5mjuFS3ZVjH3j3+g71ZsrqK8gWWFsg/pUsi7gR2IxSM17j1Rm31lb3OkyCVFO9MknrVjRlCaZbovRUAqrqdutvpTkzTFYlz97r7VkXmjalJp6SafqDRy7Q20r+mc1SVzqhGNSHLKVtep1F3IIbeSQ9FUmqfh21NnpFvEww+CzfUkn+tV/D6alLpKR64ircq2DtYHcAeDxW0owKT0MJ3p3p3vr08jG8P2l5BYXEGoBMGWTy1U5xGT8ufeqmnzXH/CJyGzQvcxpIsag4yQTgV0dNRFQYRQo9AMUJj9u3e63af3XOa0iebTNJs7a5025JVAXZFVwGPJPBz1PpVm71fR7+3a2u5xGr8FJlaInBz3APat6mvFHKCJEBB7Gm2mN1ozlzSWu+jKP9rWEWnC789Raj5Qw55HGKxrM6BrV61xBbefMVyZfLYD8+ma3jptp5KxfZ4vKV94TaMbvXFQz2xM6K8qRWYGFiXgucd/8BQmkVTqQiny3T9f8lr+BBLfLHYSzWoZBbNs2svDj2qs2qWF/JZ2erWUsFzcviOKZMjcAW4YcdF9asSOJXMsexraEYReMF+OhrXI3MCQMjp7VINxitte99f66i0VS1E6j8v9nfZv9rz8j8iKxRL4ouDcJGulwshADFnfPGemB601G5nCnzK90jqRUMyQ3MckEqrIjDa6HnPsa5a603VW0yY6zqpbj7ltGFJPoD6nitPwnpB0fT2jZ2MkrCRwcHacAYz36daWzNJ0YQhzKd35X/N2/I4nX/DEGi3CTwSmErMrW0hyxBJ5XHoK9A0HUk1SwWVSvmqdkqr0Vh1FT6hZRXsHlzKGwQy57EdK43Qb9tMmjUQDMk5jucHkHJGR9CR+FW3dWO2dSWOo+/rOP5f0vv8AU72igUVB5IUUUUAFFFI52qSaAMPxHdqVFmOr8uR2FXNHiAs42aNVOMKPQVz9pc/2tqUzL/E2wf7o711qgKoC9BwKZ2117KCpdd2SVWvElkVEjICsfnbuB7VOGFLSONaMp3LQxweSOuPlT1qjZpLpMf8ApTeZC3V+pj+vqK1Li3S4TDj6HuKx1v59PnW31MeZbOcJcgYGfRvSmb0/ei4r7v8AImntQpa90ph5jDcUX7sn/wBf3qzpGpR6jAXT5XU7XU/wn0rO1C3u7DddaYoubdhmS0Pcdyp9farHhm70+/sPN02MRJuIePGCrdwR60FVI3pc2/n28n+hNq9pJfvb25yLUtvmIPUDBC/if5VpAe2BS0Ujncm0o9gxRRRQSBpKWg0CEooxS4oASs3XNL/ta3jhNzJbhW3bowCTwR3+taeKKCoScHzIy7jSIpLW1to3eGC3dX2p/HjnB+p61Pf39vYRb7qQLnhVxksfQDvV2uU022Sx8QKmrt9ov5gxtrgjAYDllA6Ajj8KaN6a9pdze3Tq/wCupZhvNQGqxz30YgsZQY413ZIPYsMcE/1rQ1BJYHF7aoZZVXa8QON6+3uKZr2o6fZWuzUZUVZQQqZ5bHpVHwRc6tdaW0mtW/kHd+5UjDFP9rnr+VFrq5bi5U/bWSS0t39O/mQzXn9p+KbezVSI7WMzuM/x9Ap/z2rqFrA8P6NLpt/qM80iy+ewMbAYOOSc/ia6ClJK+hGJlDmUab0SX+b/ABErzn4hW02mava6pAw+zTHy5o8dWxwfxwB+Fej5rK8SaauqaNc2rAbmGUJ7MOQaqLszTA4hUKyk9tn6Mj8LX/2/S1LNuliwjn1OMg/kf51s15d4I1aKz1CwhLBRcB7aUejqSUz78kfjXqApSVmPMMO8PWa6PVC0UUUjiCsrxM7ro06wj95INi4PrWrWB4gkY3dvEn8OWNBth481Rf1sM8KWAt7cSHO4DaM1uTukUZLkKuMVHp8RjtY1bO7qabesS0S7A+WywPYUDq1HVquTKFppIjQPb3M0ZJ3cEEVaEl7BJ88aTQ/3lOGz9KYLRS+60uZbdhztA+X8jU2buP8A1gSdfVRtNMcpOT1s/UadVt0/4+S1uc4/eKQD+NWGS3vLdo22SxOMEdQagluoP9XdIU3cYccVTvLK2jtpXs3MUrjCFWOM/SgmMYtrdP7y5o+mppVktrDJJJGpJUyHJAJzjPoKsQW8NuZGhiRDI25yoxuPqar6PDd29kkd/cLcTDq6rtz+tX6CKjbk7u/n3AdKKKKRAUVzuua4+japALpVOnyocuPvKwI/PrW1Z3cN5AsttIskbdCpoNZ0ZwiptaPqWKKM0UGQU1nRWVWZQznCgnknGePwBpk8yQRl3P4etc/pxl1HWobqUDZbq+MHhWbAA9+M8+9NI0hScouT0SOlooFFIzCsPxfpk2qaLJFY7BfIyyW7ucbHDA5z9Mj8a3KDTTsXTm6c1Nbo5bRvDWZoNQ15lvNWRcb+iRn/AGR0/Ej8q6jgdOlcz4r8UHQriC3g0u8v7iZSyrApxx2JweazRdeMdZH+jW1vosTc75mErgfT/EVfI3q9EdsqFbEJVakko9LtLTyW9vRHR3OvWNreLa3jvbyucJ5q4V/o3Q/zpbua7mXztJuLSQKCGjfJBP8AvA8fkaxpfB324RvrOq3V5IhDEBVjXPsACQPxpdR8L6ZI8NlaPPYMwZ8Qk7WXI3dcgHnrUOxKp4ZWSk2+ul1+j/Aqa541vdARJNW0GcQsD+8glEgGPXA4/Gul8OaqutaPbagsE1us67hHMAGAzx/jRrTyWOgXT2UKSSQQkxxvyGwOhqzptzHeafbXUQxHNGsij0BGaptNaLUitOlOknCnyu+93+Tv+Z5J4zt10bXLhrOPYzzrcR8/eYKWJ/MV65p10t7YW9zH92aMOPxGa86+MFiwm0m+Vgqq5jcHv0I/rXT/AA+vob3w8iQStKLaRoS7LjPOR+hFVJXipHpY9e2wNGut1o/y/T8Tp6KBRWR4QVzl7tm1aQ7s7cJ+n/166OuM05/M1+6Y95iMfTig7MJH4pdkdkvSqF7YtNIZLe4aCb+8Bn8MVoUUHLGbi7oxn/tKNClxBHdJ/fjfy2FQwyaaxMEyy283dZmbP5k1vU1lUjDAEUzVVV1VvTT/AIBTijkXC7hPD23dvxqnqPh2zu5hMhkt5wQd8bf0PFSXujRyAtZzS2c3XzIjn8weKZpltrFvd4vbuC6tthAZYyjA/Tn+dCbRSk4rnpzs/u/4BsgetLRRSOYDSUppKAMXxbp6ahpEoYEvFiRdo54PIH4VzHhxfs12JrFjtkBLgnhvcj1r0AgEEHkd6x7LTksZGjhChWJwB2FO9jvw+K5KMqUjTinVgu7AY4496r3+pRWjLGfmnY/Kg/zxS3MXmfJ7A+prKLFRIzx8Akf72KRjTpxk7ktxOEjmNw7kD5ycfcB6AGrWgIRZmVs5lbeB6DAH9KjQqYEXbiMhcHPXIrVjQRxqijAUYFNhVklDlQ+iiikcwUlLSUCYjMEUsxCqBkknAAqq2pWS2a3jXUItmGRKXAUj60upWa39hPayM6LKuNyHBHuK5fTPh5o1pavBdedfxu+/bO3yg+wXAqko21Z00oUXFurJp9kr/qi23jnRGcx2dw95IDjbboXOfy5/CqWr6/NA1jql3Y3FpYW7lpZGGTtYbeV6jkj8q6mxsLTToFhsbeKCNRgLGuBU80STRmOVVeM9VYZBpOz2NVWoQl7sG15vX5WWn4kdrPFf2kU8R3wTIGXIxuUj0rF8F6bqelWNxbanNHJCkzC0VRykXYE+v8q6FVAAAGAOgpaDm9o1FwWz/Q434qWwuPDakgkxzow9snGf1rO+EThE1m15ykyyY7cgjj2+Wuu8UQpcaDeRStsRk+Y+gBBNeefCiSaHxPqto3EJj+XHTKsQf51qneDR7WGftMsqw/ld/wAUes0UDpRWJ4IVxWgq82tSMjfu1dmY/j0rta43S3FvrJtVkDbX3Ow7kknFB24X4KiW9jsJHCKWcgKBkknpWfJPdST4svJaHb97d3q/NGksZSQBkIwQe9Y32O6s70DTIYI7XHO5sZP0oOakk73/ABHs2piOSR54lCHgeXnI/OotQuIpZrbdfNEpHRBjJqZre8mRlS6i2nqAvX1pt9BfL9la0S2LJncz8ZpnRHlurtX/AOB6GdcRWablN5e3Ey8jqf5ACpdP14JdWmnpb31yXyrXDQkKvGeTjFaYN8SEmmijY9Cgzn86s2EdzHbqLyVZZucsq7QefSgJ1IuFpK/z/wCAWhRQKDSOMQ0UVFNPHCu6Rgo6D3PpQG+iJCQoJY4AqqJUklyh9s4rL1GeWaRlbiDpt9enNM067kaWVZQoijJCgDB/Gg6o4d8vMzfC/PuJqtc20bwthih6gjnB9acZUZ1+bHBOPT3qvcKskTsxIAO5eeopmcItMhuhi6iPmYVVB5HcVoxXcTsUztYcYIrKiiaR1l8/bEgBPfNSXN1BKk25lX7rBsdfSkayp81kbIorz6PxgukxxpeAyxmTYzFseWD3rrdF1zTtYRm0+6jmK/eVW+ZfqKdm9UKvgq1Fc0l7vc1KSlpKRyCOyquWOBWB4osXljS/tt7T23z+UD/rBjp7dc1vkAjkAj3rIlmk0dmaVvM085PP3ov8V/lTW5vh5SjO8d/zKUVwbyCGaK+vbcMMnbGHXPTqQfSpBJNGG/4nSsWTC+bCBj34xVyw1Kx8pUsxIyMxIVUJwTyf50puJZZlZ7WZYY2yG2gluMfd64p7mzlZtctvW36oW21K2gsEa81C3dlBDy7gAWHWk8M3kuoaNFdzMjGZnZCgwNm47f0xVe+udHvISlwbctghUm+Q5/HmpvCsNzb6BaQ3kcUcqLtCxPuULk45+mKHsROMVTbatK6/XYk8RGAaHem8VntxEd6qcEj2PrXD+DLCDT/Gz/ZJ3mt7qzNwhf72GYdRXYeMiV8L6iVOH8oheepJ4FYHh6KJPGrJGXL2tgtu5Zccjb/jVRejR14WUo4apZ6NO/yt/md1RQKKzPLCvPLJfL8WXH90T/zGa9DrzbWxLbeOJHTiFgkh/LFB6eWrmdSHeLPRpFLxsoOCRjPpWFY2WsWKMs15HfqSSA6bMD071uwtujU+op9BwRqOF42Wvl/VjmUhuGvgsFm1moXDyIQQSfSoNZh8RxxRiyMN6FcNziN+O3XFdZxRTTsbRxTjJPlTt31/4P4mVqNvc3mlKE2xXqqGGTwrfWrmni6W0jF66POB8zIMAmrNFIwc248oUUtBoIIrgyCBzAqtKFOwMcAntk1xkujeI7zUorq5uLNNgyFyXCnvgYFdxSVSlY3o4iVC7ilr5XMIaRdyTK896rKBgosWB+easWWkm3A33Bc8/dQLitWilcHiKjVr/gjnboSWuomMfvDIMxg/WrDSM8TDBR4uNvQYqfXIlESXBYJ5WfmPbIx/PFZFzIJQsKGN5ZGVXO/BH0pnTTtUin/X9WNSw0y2MCNIhcsM4YkgfT0qSTRNNk4ks4nx/eGa0FUKoUdB0p1ScTrTu2mzL/4R/SNpT+zLTaexiFWbLTrOwJ+x2sMORg+WgXP5VbpadxOpNqzYCkpaSkQzJ8UpqUmh3K6LJ5d9geWcD1GevHTNUtH8NxW9qn9qP9uu9mHaTkDPUAdx9ad4nlv2KRaY7LMFLFAQGkXp8pPcH+dWdH1q2u1EDu0V5GAJIphtYH8ev4VSeljui6kaFobXu7b/AD8iW50WynuIZvLMcsRyrxsVP444P41ReLXra8QwzwXdnu+ZXXbJtx0HOCc9yau2uuWdzq8+mo7C7iG5lK9uOQfxrUpbGTqVKfuzXTr+n/AOUtV07XtQvbXVtPiN9CFLxt84VSOMN0qS18IQWniKPU7e8uUhRTi03HZuxjPX9K6VY1RmZVUFuWIGMn3p1FypYqeqg7Jq1t0Yfi/EmnwW5AInuYkI9gwP9KyvB8UU3iXX7+OTesjRopHQcEnH5j8q0PErGW/solKo0SS3AdmwAwXaoP8A30T+FUPhjph0/RbiV5oJnu7h5WaAkqCPlxnjOCD0qlZRbOiDUMJK71dl97u//SUdiKKKKg80K4L4i27xXdheRnAJMb/oR/Wu9rC8ZWA1DQZ02lnjxIgHqDQjrwFZUcRGT2/zLeg3C3OmQurbuMZqLV9QmgiVbZV+0mQIEJ5Iz1rL8ETotp5CntlRXSSpEGEsioGX+Jh0/GmFeEaNeSaurjrd2eJWddrEcipKo2WqW95dzQWzeZ5QG51yVz6Z6Zq9ikcs4uL1VhaKKKBBRRRQAUUUUAFFFFAFbUbZL2yntpfuSoUP415J8PZri/8AG7QXGStkrsxPUuPl5/M17JWHpOhQ6druqajFjN5s4xyuAc/mTmtYTUU0z0cHjI0KNanJayWnrfX8L/cbg6UYoorI84MUUUUAFBorjfE2gW3iPVHW21e4tr+0C7o0bcoyMjK5/UVUUm9WbUIQqStOVl3tc6TUrNbpY2B2Twtvif0P+BrPtvsWv2LLdwqJF+VsHke6kc1S0rRtYs7TyxqB+Uj5JFDq474PUA/SoJ3023CQa3Y/Y2c7RMhOxieeq9M+9B0xpqN1CV7bNb/dp+BXg0dvC2opc2Kyah9pYoFb74GM/e79PTtXQSeIbVYZVTDXibR9lLYbc3AB9Kr2Ym0/e8jRXWnlzJHLGoUxA5zn169ahu9Os/EssFyLlJI4gQxhOG3emRRe+5c5xrS5q+tvtfkmvw6HT0Vy041vSLm0h0+I6lZSOEbzHCvCvclu46/yroL+7jsLKa5nP7uJSxpW0ucU6TVuV3vt/wAN0ON8Z38UTXkjMknlxrEkW4gl88811WgWP9naLaWhUK0aAMB/e6k/nmvMdMRvEHi23tpJgqowuZVRQPuknBPudv616/8AShnoZlBYeEMPfXd/kv1+8WiiikeUFNdQykNyDTqDQBxGnWzaRqkiHhFYlfoa6rULKDVbCS3ul3wSjkVk+LrZ2hhuIgco2Hx/dq5oV+k8XlZ+dOOtM9Gu3WhGvHdbi3mpWejiC1VHaRh+7ggTcxA74HatKKTzY1cBlyM4YYI+orPv47qBnm0+2hnuZAFzI+3aAPXHT2p1tmwtU+2SbpZW+dwMDcf5Cg5JRTimt/zNKiiikYhRRRQAUUUUAFFFFABRiiigAooooAKKK5nxJpCa/LH9lu3t7zT5DhkJ4LKDg8+mKDSlCM5Wk7LvuY/izVtWn1ryPDxZvsC+ZcgEYJIJCnPt2rZuNLXVYLfWLIi21ZoVKS8kYIztIzyO1M8I201lYtZ3lh9nuC5Z5VYOsxz94kdD9ateGJWg014LuRd9ozIzf7OeD+VaSlokjvrVFBctJL3NLr7Se9+6v07Oxd0i/a9E6Sx7JYH8tvc4zmr0kaSIVdQVPUEVmGdIrmK6VXWGc+U+4Y5/hP49Pyq1Y39reNMttKrvC5SQDqp9DUnDOOvNFWRzmreGbqKGKLw/OLeAybpYXc7MZzx1P4dOauaz4Ziu3+1afM2n6gGDedEOG9mXoeK6MUlJSadzRYyqmmnt+N+/f5mL4XOsfY5V16KJJ0fahjYHevqcdKyPiReMmmfZYm2lwXkJzgKK6bVb6LTbCa7uD+6iGT7+1eXXk93r2qx2y71N6fm4/wBXHgZP0xT31O7L6TrVniZJKMdfL+upt/CXSXhs7rVbmILLeMBEe4jXP8zn9K9BFQWVvHaWcVvCNscShFHsKsUpO7ucOMxLxVaVV9fy6fgFFFFI5gooooAZNGssbIwyCMYrz2KabQfEBtX6M2VP95T/APrr0WsXxNpA1O1DR4FzD80bf0PtQduDrqnJwn8Mv6uakEizRB05BrOvbPUbqV1W/jt4DwBHCC+PqTj9K5nwnrH2NmtdQYrMX2lD1U13LjzImCtgMD8w7UyK9GWFqW6dHYoWeoWa3BsI7sTXEK/OOpH+8RwK0wc1y8VnPZWsGkafayKG5nu2xjrknryTW3LqVnFdi1lnRbggNsPXHY0EVKSv7mv9b+XoXaKAeKKRgFFFFABRRRQAUUUUAFcL4X/thb/Ur2/vFWw8wsVkTkAZ4HPAxjn9K7quS0yF7fxVrNhOwe3ulWeME8jI5H55q47M7MLK0KkbLZfddXt5k114pgM1l9hVpYJZQkkhjcKAc4wcYzmrekqYtf1tdm1XaKUN/eJTB/lWVphik0a70gFri4t3dVCDlRuJU1b+2zW+q6VLco0Qu0aFwwwQ46Z+tLToazpxXNCmuj36r4k/uRv3U8VrA887KkaDLMewrlY/OuvM1a2jjexMm9Yl5aQKSC+e568e1dgQCCCMg9Qaz9XvItI0ma6MTtHCAdkS5PJx0/GpOWhNx92Ku3p/wCK4v7CbSDcSTKtrJhVbnIbOAAOuc9qozrpGmXEOq6my2t5IfLaYsyB2wRyBwePWuWMU2jm08Sqgm0lwZJLZUO6HeR84ye309a7ie30zxJpCedHFeWMwDLnkH39jV2sdNSlGhZptwb1a/Fev5mlFIksSyROrxsMqynIIp1Z+jaPZaNa/Z9Nh8mEncV3s3P4k1h+OvEB0u0FrbEm6nyOOqgc5qXa+hzU6DrVfZUtb/L/MxviRqIuY0t4nYQQuC5U8FvQ+w5rU8BaRPb2g1DUUC3tyiqq9441HCn3PU1m+G9Ii1wx3EqSCxhfOC/8ArHU9PoD1/L1r0JRineyPSxmIjQorB0+m7/T/AD+SHDpRRRUnjBRRRQAUUUUAFBoooA5PxToCS3K6nboWnjH7xAfvj/Gm+F/EcV1uhmYLg4XNdcRmuN8R+GSt2dU0pSLjrJCOj+496aPTw9enWh7DEP0fbyOx+lYd3Yzz6tbSSRQ+VGpDzZ5cEfdxWfoniFVK29y24rweMFfrXVRyJMgZGDKfSjY5ZwqYWTTRhxa4ZdQubeytDJa2vE1wX2qrYztAwcmtm1mS4t45ozlHUMv0NYd9oU8lrLZWk6QWdxI0k5C5chjlgDnjNPUy/wBqzW4f7PpdhEvyqcbyR69QBimOUITXuf1t+LfRG9Sisvw/ftf6PFduhUPuZR1JXcdp/EYqzp99b38LS2kgkRWKEjsR1FKxhKnKDafTQt0UDpRmkQFFFGaACuU8bXCaS1jrDSCP7PJsk4++jcEfh1rq81HcQx3ETRTxrJGwwVYZBpp2ZrQqKnNSauuvp1MawupbwLdWFgsSTKGM0xAZh2OByazPFWl3C6VLf/bJZbq0ImjBwqDB5GB7fyq1bajeT6ld6ZB5Fu8GNu5ScJ2IA61d/s+GG0lbV7g3IYEu0xCoB6ADgCmdKl7Copbbabtr/hi9pd4moafBdR/dlQNj09qtV598MPENrdm90WGRW+xOxhI/jiLH+X9a9Bpzi4uzMMXh5Yes6clb/Lp+AyeJJ4XimUPE4Ksp6EGsXwj4btPDNlLa2UtxJHLIZD5z7tpPYegrdrktY8YWlv5kdo4lKD55FPyj6e9JJvYqhCtWTpUrtO1/ltc0PEetfYF8i1CvdsMgH+EeprirHQ5vEeozLK8i2qPmWTI7ryo9+ce1P0rTrnxK7PbyPDYOR51wwyz+qrn+fSvSbK1hsrdILdAka9AKPhXmejOrHL4ezp/xHu+39f8ADhaW0VpbxwW6BIoxtVR2FTiiipPFbbd2FFFFABRRRQAUUUUAFFFFABQaKKAMTVtAt71/OhxFc/3gOv1rHbU5tGPlXC+T1ILcqw9q7Oqt/ZQX9u0NygdG/T6U7nXRxVrQq6xM/SPENlqKqElQSHjaTyfpV2/0621CPy7uPzF9MkA+xx1Hsa821nwJfadfpd6RK88SksQSA6/41o2fim8sSiXCPJx8yuNpWqa7HfUy+E7VMDO/ls0dpqiXUOjzppEcf2oJiFWO1Qe1VvD1sNG8O28VwvleVHmUk5+YnLEke5NTwaxaSW0Mskgj8xtoB9fSryPHIPkZX+hzU+p5jc4RdOa66/I5bRLiG71hG0O4LWCl/tLPKX3t2ABOR65rT1PVHtbmdZJEtreFA/mSLkPnqPbFaUdnaxzGVII1lIxuCgHH1qLUtMttSjEd5GZIhzs3kKfqAefxoui3WpzqJtafj/wWVbLWDeeHE1OC2kdnj3rCOGbnGBkd6i8L6vJq8UskqrE64BgKlXjPPXPWtWWJjb+XA/knjBUA4/A1WstNjtrmW6eR5rqRQjSPgfKM4AA4HWixPNT5Jaat6eX9fec5c+KJbK3W5vBiV7oWy2YTnlsDn1xz6V2h6VSnsIJ7pLiZTK8ZygY5CnGMgeuO9XD0pyt0FVqU525Fbv8A15HD+J9C1qXxTaapoVwkQZBBOWwSi5yWAPWt+00G0jcTXJkvLnvLO24/gOg/AVqswVSzEBR1JOKxta8TabpEHm3M2/PACDOT9elPmlK0Ube2r11GlBXa00WvzfU1I7S3jcvHBGj4wWCgHFRanqlrpsPmXMqqOw7mvOtT+Joktpn0+HyzGcFm+bH9Kp+GtO1jxM3nXNqYbZhn7RMMfkven7OSV5HdDJ6kI+1xj5Irvuy/qfj+SW5C2sKywMCrQ/3s++P0q9pngWxvXS9ljkgs7iIF7Js5U/3Sc5x3rf8ADHhGw0BS6bri7YktPL1+gHQCuixU81vhIr4+FL93gU4rq+/y/XcjtoIrWBIbeNY4kG1UUYAHoKlpaTFSeO227hSiiigAooooAKKKKACiiigAooooAKKKKACjtRR2oASqd9ptpfLi5hV/erlFCdhxk4u8XZnLzeF2jtbm3s7lhG6nYsgzsfsd3WuHuLLxNoxkM1vcXEY+YGBtw/Eda9goqubuejQzOpSupJST7r9TyfR/G2pCVILwmORsfLIu0/Tkda6eLxVN5g3RIUP8XK11ssEMoIkjRgfUVn3ug6XeHNxZRN7gYP5iiVntoaVMZhKsrujy+j/4YyJvGFvAF81ByccN/wDWqnN4/tkjdhCDtbb9/GfpxWnJ4K0GTObHrz/rX/xpn/CC+HiMGw3jP8UznH05ojy9So1MsXxRl+H+Zz3/AAseSSRhFZoqgZLFs4/lUN94+MsD+RcRqTGSNvUH6da6628H6BbOHj0uDeO7ZY/rWxb2dtb8QwRR/wC6oFVeKHLF4CDvSot+r/4c8c05fFWqpJIba8mVjlTIfL3D23VtHwBqmtW4TWr/AOxwbgwigAdsehb/AOsa9Rop+1ad4qwVM7rf8uYxh6L/AD0/A5nQ/BWiaNtkgtPMnX/lrMxdv14H4V0gAGABgCnUVnKTluzyq1erXlzVZOT89RaMUUUjMKKKKACiiigAooooAKKKKAP/2bhK9z8AAAAAeKC+XxLC/Y4zurX+ofzrFw==";
                    printBitmapTest(ss);
                }
            },1000);

    }



    /**
     * 打印几遍
     * @param num
     */
  private void printTesttwo(int num) {
        try {
            ArrayList<byte[]> bytes = new ArrayList<byte[]>();
            for (int i = 0; i < num; i++) {
                String message = "蓝牙打印测试\n蓝牙打印测试\n蓝牙打印测试\n\n";
                bytes.add(GPrinterCommand.reset);
                bytes.add(message.getBytes("gbk"));
                bytes.add(GPrinterCommand
                        .print);
                bytes.add(GPrinterCommand.print);
                bytes.add(GPrinterCommand.print);
            }
            PrintQueue.getQueue(getApplicationContext()).add(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void print(byte[] byteArrayExtra) {
        if (null == byteArrayExtra || byteArrayExtra.length <= 0) {
            return;
        }
        PrintQueue.getQueue(getApplicationContext()).add(byteArrayExtra);
    }


    private void printBitmapTest(String singUrl) {
        Base64PrintUtils.INSTANCE.printSealImage(singUrl, bitmap -> {
            try {
                PrintPic printPic = PrintPic.getInstance();
                printPic.init(bitmap);

                // 及时回收 Bitmap
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                byte[] bytes = printPic.printDraw();
                // 创建优化的打印指令序列
                ArrayList<byte[]> printBytes = new ArrayList<>();
                // 3. 反向走纸2行（回到最后2行文字的位置）
                // ESC d N - 反向走纸N行
                printBytes.add(new byte[]{0x1B, 0x64, 0x02}); // 反向走纸2行

                // 4. 设置绝对水平位置（从行首开始）
                printBytes.add(new byte[]{0x1B, 0x24, 0x00, 0x00}); // 从位置0开始

                // 2. 设置高质量打印模式（关键步骤）
                printBytes.add(new byte[]{0x1D, 0x21, 0x00}); // 设置双倍宽高模式
                // 3. 设置行间距（避免图片被压缩）
                printBytes.add(new byte[]{0x1B, 0x33, 0x00}); // 设置行间距为0

                // 4. 居中对齐
                printBytes.add(new byte[]{0x1B, 0x61, 0x01}); // ESC a 1 - 居中对齐
                // 6. 打印图片数据（使用光栅位图命令）
                printBytes.add(bytes);
                // 8. 恢复左对齐
               // printBytes.add(new byte[]{0x1B, 0x61, 0x00}); // ESC a 0 - 左对齐
                // 9. 添加换行和切纸命令
                printBytes.add(new byte[]{0x0A, 0x0A, 0x0A});
                printBytes.add(GPrinterCommand.print);
                Log.d("BtService", "图片数据长度: " + bytes.length);
                // 合并所有字节数组
                byte[] allBytes = combineByteArrays(printBytes);
                PrintQueue.getQueue(getApplicationContext()).addTwo(allBytes);
            } catch (Exception e) {
                Log.e("BtService", "打印异常", e);
            }
            return null;
        });
    }


    // 合并多个字节数组
    private byte[] combineByteArrays(ArrayList<byte[]> arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int currentPos = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentPos, array.length);
            currentPos += array.length;
        }

        return result;
    }
//
//    private void printPainting() {
//        byte[] bytes = PrintPic.getInstance().printDraw();
//        ArrayList<byte[]> printBytes = new ArrayList<byte[]>();
//        printBytes.add(GPrinterCommand.reset);
//        printBytes.add(GPrinterCommand.print);
//        printBytes.add(bytes);
//        Log.e("BtService", "image bytes size is :" + bytes.length);
//        printBytes.add(GPrinterCommand.print);
//        PrintQueue.getQueue(getApplicationContext()).add(bytes);
//    }

}