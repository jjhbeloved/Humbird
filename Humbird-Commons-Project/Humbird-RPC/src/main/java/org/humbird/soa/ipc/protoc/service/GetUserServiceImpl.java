package org.humbird.soa.ipc.protoc.service;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import org.humbird.soa.ipc.protoc.vo.LeeInfo;

/**
 * Created by david on 15/6/7.
 */
public class GetUserServiceImpl extends UserService.GetUserService implements UserService.GetUserService.BlockingInterface {

    @Override
    public void getConfig(RpcController controller, LeeInfo.LeeRequest request, RpcCallback<LeeInfo.LeeReply> done) {
        if(request.getVersion().toLowerCase().equals("hello")) {
            LeeInfo.LeeReply.Builder builder = LeeInfo.LeeReply.newBuilder();
            builder.setHttpServerIp("1.1.1.1");
            builder.setBinaryShareIp("1.1.1.2");
            builder.setPlayerType(LeeInfo.LeeReply.PlayerType.BCM97208);
            builder.setTcIp("1.1.1.3");
            builder.setTcIf("eth0");
            done.run(builder.build());
        }
    }

    @Override
    public LeeInfo.LeeReply getConfig(RpcController controller, LeeInfo.LeeRequest request) throws ServiceException {
        LeeInfo.LeeReply.Builder builder = LeeInfo.LeeReply.newBuilder();
        if(request.getVersion().toLowerCase().equals("hello")) {
            builder.setHttpServerIp("1.1.1.1");
            builder.setBinaryShareIp("1.1.1.2");
            builder.setPlayerType(LeeInfo.LeeReply.PlayerType.BCM97208);
            builder.setTcIp("1.1.1.3");
            builder.setTcIf("eth0");
            return builder.build();
        }
        return builder.setHttpServerIp("999.999.999.999").build();
    }
}
