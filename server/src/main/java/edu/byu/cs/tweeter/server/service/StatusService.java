package edu.byu.cs.tweeter.server.service;

import edu.byu.cs.tweeter.model.net.request.FeedRequest;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.FeedResponse;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IStatusDAO;

public class StatusService {
    IDAOFactory daoFactory;

    public StatusService(IDAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    public StoryResponse getStory(StoryRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }
        daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken());

        return getStatusDAO().getStory(request);
    }

    public FeedResponse getFeed(FeedRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }
        daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken());

        return getStatusDAO().getFeed(request);
    }

    public PostStatusResponse postStatus(PostStatusRequest request) {
        if(request.getStatus() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a status");
        }
        daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken());

        PostStatusResponse response = getStatusDAO().postStatus(request);

        // propagate the status to all followers

        return response;
    }

    private IStatusDAO getStatusDAO() {
        return daoFactory.getStatusDAO();
    }
}
