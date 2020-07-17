import axios from 'axios';
import { ActionTypes } from './store';

export const getAuthAction = (
  projectId,
  clientKey,
  channel
) => async dispatch => {
  try {
    const response = await axios.get(
      'https://builder.wisevoice.ai/api/registerAnonymous',
      {
        params: {
          projectId,
          clientKey,
          channel
        }
      }
    );

    const { token: authToken, user_id: userId } = response.data;
    dispatch(getAuthenticatedAction(authToken, userId));
  } catch (error) {
    console.log(error);
  }
};

export const getAuthenticatedAction = (authToken, userId) => ({
  type: ActionTypes.AUTHENTICATED,
  authToken,
  userId
});

export const getSendMessageAction = giftedChatMessage => async (
  dispatch,
  getState
) => {
  dispatch(getMessageSentAction(giftedChatMessage));

  try {
    const authToken = getState().chat.authToken;
    const response = await axios.post(
      'https://builder.wisevoice.ai/api/process_message',
      '',
      {
        params: {
          message_options: giftedChatMessage.text,
          timezone: 'Europe/Bucharest',
          last_node: '0',
          language: 'ro',
          returnSyncResponse: 'True'
        },
        headers: {
          Authorization: `Bearer ${authToken}`
        }
      }
    );

    const botResponse = response.data;

    console.log('~~~', botResponse);
  } catch (error) {}
};

export const getMessageSentAction = chatMessage => ({
  type: ActionTypes.MESSAGE_SENT,
  chatMessage
});

export const getResponseReceivedAction = response => ({
  type: ActionTypes.BOT_RESPONSE_RECEIVED,
  response
});
