package edu.byu.cs.tweeter.server.service;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowToggleRequest;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowersCountRequest;
import edu.byu.cs.tweeter.model.net.request.GetFollowingCountRequest;
import edu.byu.cs.tweeter.model.net.request.IsFollowerRequest;
import edu.byu.cs.tweeter.model.net.request.LogoutRequest;
import edu.byu.cs.tweeter.model.net.response.FollowToggleResponse;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.FollowingResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowersCountResponse;
import edu.byu.cs.tweeter.model.net.response.GetFollowingCountResponse;
import edu.byu.cs.tweeter.model.net.response.IsFollowerResponse;
import edu.byu.cs.tweeter.model.net.response.LogoutResponse;
import edu.byu.cs.tweeter.server.dao.IDAOFactory;
import edu.byu.cs.tweeter.server.dao.IFollowDAO;
import edu.byu.cs.tweeter.server.dao.dynamodb.DynamoDAOFactory;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService {
    IDAOFactory daoFactory;

    public FollowService(IDAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    /**
     * Returns the users that the user specified in the request is following. Uses information in
     * the request object to limit the number of followees returned and to return the next set of
     * followees after any that were returned in a previous request. Uses the {@link IFollowDAO} to
     * get the followees.
     *
     * @param request contains the data required to fulfill the request.
     * @return the followees.
     */
    public FollowingResponse getFollowees(FollowingRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        return getFollowDAO().getFollowees(request);
    }

    public FollowersResponse getFollowers(FollowersRequest request) {
        if(request.getUserAlias() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[BadRequest] Request needs to have a positive limit");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        return getFollowDAO().getFollowers(request);
    }

    public FollowToggleResponse follow(FollowToggleRequest request) {
        if(request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        String currUserAlias = daoFactory.getAuthTokenDAO().getCurrUserAlias(request.getAuthToken());
        User currUser = daoFactory.getUserDAO().getUser(currUserAlias);
        FollowToggleResponse response = getFollowDAO().follow(request, currUser);

        daoFactory.getUserDAO().incrementDecrementFollowCount(currUserAlias, true, "following_count");
        daoFactory.getUserDAO().incrementDecrementFollowCount(request.getFollowee().getAlias(), true, "followers_count");
        return response;
    }

    public FollowToggleResponse unfollow(FollowToggleRequest request) {
        if(request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        String currUserAlias = daoFactory.getAuthTokenDAO().getCurrUserAlias(request.getAuthToken());
        User currUser = daoFactory.getUserDAO().getUser(currUserAlias);
        FollowToggleResponse response = getFollowDAO().unfollow(request, currUser);

        daoFactory.getUserDAO().incrementDecrementFollowCount(currUserAlias, false, "following_count");
        daoFactory.getUserDAO().incrementDecrementFollowCount(request.getFollowee().getAlias(), false, "followers_count");
        return response;
    }

    public GetFollowersCountResponse getFollowersCount(GetFollowersCountRequest request) {
        if(request.getTargetUser() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        return daoFactory.getUserDAO().getFollowersCount(request);
    }

    public GetFollowingCountResponse getFollowingCount(GetFollowingCountRequest request) {
        if(request.getTargetUser() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a user alias");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        return daoFactory.getUserDAO().getFollowingCount(request);
    }

    public IsFollowerResponse isFollower(IsFollowerRequest request) {
        System.out.println(request.toString());
        if(request.getFollowee() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a followee");
        }
        if(request.getFollower() == null) {
            throw new RuntimeException("[BadRequest] Request needs to have a follower");
        }
        if (!daoFactory.getAuthTokenDAO().authenticateCurrUserSession(request.getAuthToken())) {
            throw new RuntimeException("[BadRequest] The current user session is no longer valid. PLease logout and login again.");
        }

        return getFollowDAO().isFollower(request);
    }

    public static void main(String[] args) {
        List<User> usersToAdd = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            String firstName = "Batch" + i;
            String lastName = "Test" + i;
            String userAlias = "@batch" + i;
            String imageUrl = "https://tweeterapp340.s3.amazonaws.com/%40l";
            User user = new User(firstName, lastName, userAlias, imageUrl);
            usersToAdd.add(user);
        }

        IDAOFactory daoFactory = new DynamoDAOFactory();
        daoFactory.getFollowsDAO().addFollowerBatch(usersToAdd);
    }

    /**
     * Returns an instance of {@link IFollowDAO}. Allows mocking of the FollowDAO class
     * for testing purposes. All usages of FollowDAO should get their FollowDAO
     * instance from this method to allow for mocking of the instance.
     *
     * @return the instance.
     */
    IFollowDAO getFollowDAO() {
        return daoFactory.getFollowsDAO();
    }
}
