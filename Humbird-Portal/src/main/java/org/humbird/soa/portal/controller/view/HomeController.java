package org.humbird.soa.portal.controller.view;

import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.model.IndexModel;
import org.humbird.soa.core.util.HumbirdUtil;
import org.humbird.soa.core.util.KeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by david on 15/3/20.
 */
@Controller
@RequestMapping("")
public class HomeController {

    private final static Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String version;
        Map rets = new HashMap();
        ModelAndView mav = new ModelAndView();

        HumbirdSession session = null;
        try {
            session = HumbirdUtil.getSession(KeyUtil.INDEX_KEY);
            IndexModel indexModel = (IndexModel) session.getValue();
            if((version = indexModel.getVersion()) == null) {
                InputStream inputStream = request.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
                Manifest manifest = null;
                try {
                    manifest = new Manifest(inputStream);
                } catch (IOException e) {
                    LOGGER.error("Could not load MANIFEST.MF from /META-INF/, ", e);
                }
                Attributes attributes = manifest.getMainAttributes();
                version = attributes.getValue("Specification-Version") + "-" + attributes.getValue("Implementation-Version");
                indexModel.setVersion(version);
            }
            mav.setViewName("index"); // 设置返回的文件名
            rets.put("props", indexModel.getValues());
            rets.put("version", version);
            mav.addAllObjects(rets);
        } catch (Exception e) {
            e.printStackTrace();
            mav.setViewName("404");
        }

        return mav;
    }
}
