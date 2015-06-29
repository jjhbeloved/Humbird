package org.humbird.soa.portal.controller.view;

import org.humbird.soa.common.model.common.PropsModel;
import org.humbird.soa.core.model.PropertyModel;
import org.humbird.soa.core.HumbirdSession;
import org.humbird.soa.core.util.HumbirdUtil;
import org.humbird.soa.core.util.KeyUtil;
import org.humbird.soa.portal.view.FolderModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by david on 15/3/20.
 */
@Controller
@RequestMapping("/custom")
public class CustomController {

    private final static Logger LOGGER = LoggerFactory.getLogger(CustomController.class);
    private final static String PROPS_SUFFIX = ".properties";

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView index() throws IOException {

        Map rets = new HashMap();
        ModelAndView mav = new ModelAndView();
        mav.setViewName("custom/index"); // 设置返回的文件名
        HumbirdSession session = null;
        try {
            session = HumbirdUtil.getSession(KeyUtil.CUSTOM_KEY);
            Map<String, Map<String, PropertyModel>> maps = (Map<String, Map<String, PropertyModel>>) session.getValue();
            Iterator<Map.Entry<String, Map<String, PropertyModel>>> iterator = maps.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Map<String, PropertyModel>> entry = iterator.next();
                Map<String, PropertyModel> propertyModels = entry.getValue();
                Iterator<Map.Entry<String, PropertyModel>> iter = propertyModels.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, PropertyModel> modelEntry = iter.next();
                    PropertyModel propertyModel = modelEntry.getValue();
                    String path = propertyModel.getPath();
                    rets.put(propertyModel.getPath(), new FolderModel("/custom/" + propertyModel.getFolder(), propertyModel.getFile(), "/custom" + path.substring(0, path.length() - PROPS_SUFFIX.length()), propertyModel.getKey()));
                }
            }
        } catch (Exception e) {
            //  todo
        }

        mav.addObject("maps", rets);
        return mav;
    }

    @RequestMapping(value = "/{folder}/{file}*", method = RequestMethod.GET)
    public ModelAndView list(@PathVariable String folder, @PathVariable String file) throws IOException {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("custom/custom");

        HumbirdSession session = null;
        PropsModel propsModel = null;
        try {
            session = HumbirdUtil.getSession(KeyUtil.CUSTOM_KEY);
            Map<String, Map<String, PropertyModel>> maps = (Map<String, Map<String, PropertyModel>>) session.getValue();
            Map<String, PropertyModel> models = maps.get(folder);
            PropertyModel propertyModel = null;
            if((propertyModel = models.get(folder + "/" + file + PROPS_SUFFIX)) != null) {
                propsModel = propertyModel.getPropsModel();
            }
        } catch (Exception e) {
            // todo
        }

        mav.addObject("props", propsModel);
        return mav;
    }
}
