package com.harlownk.easytodoj.api.toy;

import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String sayHello(@RequestParam(value = "name", defaultValue = "World") String name) {
//        int times = Integer.parseInt(count);
        int times = 1;
        StringBuilder builder = new StringBuilder();
        String val = "Hello " + name + "!";
        for (int i = 0; i < times; i++) {
            builder.append(val);
        }
        return builder.toString();
    }

    @PostMapping("/jsonapi")
    public TestObject sayJson() {
        return new TestObject("one", "two");
    }

    class TestObject {

        String start;
        String end;

        public TestObject(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public String getEnd() {
            return end;
        }
    }


}
