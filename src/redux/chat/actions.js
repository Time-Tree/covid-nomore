import axios from 'axios';
import { ActionTypes } from './store';

import keys from '../../../keys';

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

    console.log('Response from WiseVoice', response.data);
    const { token: authToken, user_id: userId } = response.data;
    dispatch(getAuthenticatedAction(authToken, userId));
  } catch (error) {
    console.error(error);
  }
};

export const getAuthenticatedAction = (authToken, userId) => ({
  type: ActionTypes.AUTHENTICATED,
  authToken,
  userId
});

export const getInitBotAction = () => async (dispatch, getState) => {
  try {
    const latestInit = new Date(getState().chat.latestInit);
    const now = new Date();

    // Execute only once per day.
    if (
      latestInit !== null &&
      latestInit.getDate() === now.getDate() &&
      latestInit.getMonth() === now.getMonth()
    ) {
      return;
    }

    dispatch(getBotInitStartedAction());

    // Authenticate and store access token
    await dispatch(
      getAuthAction(
        keys.WiseVoiceKey.projectId,
        keys.WiseVoiceKey.clientKey,
        keys.WiseVoiceKey.channel
      )
    );

    const { authToken } = getState().chat;
    const response = await axios.post(
      'https://builder.wisevoice.ai/api/events/trigger',
      '',
      {
        params: {
          returnSyncResponse: 'True',
          eventNodeTypeId: 1
        },
        headers: {
          Authorization: `Bearer ${authToken}`
        }
      }
    );

    dispatch(getResponseReceivedAction(response.data));
  } catch (error) {
    console.error(error);
  }
};

export const getBotInitStartedAction = chatMessage => ({
  type: ActionTypes.BOT_INIT_STARTED
});

export const getSendMessageAction = giftedChatMessage => async (
  dispatch,
  getState
) => {
  dispatch(getMessageSentAction(giftedChatMessage));

  try {
    const { authToken, lastNode } = getState().chat;

    const response = await axios.post(
      'https://builder.wisevoice.ai/api/process_message',
      '',
      {
        params: {
          message_options: giftedChatMessage.text,
          timezone: 'Europe/Bucharest',
          last_node: lastNode,
          language: 'ro',
          returnSyncResponse: 'True'
        },
        headers: {
          Authorization: `Bearer ${authToken}`
        }
      }
    );

    dispatch(getResponseReceivedAction(response.data));
  } catch (error) {
    console.error(error);
  }
};

export const getMessageSentAction = chatMessage => ({
  type: ActionTypes.MESSAGE_SENT,
  chatMessage
});

export const getResponseReceivedAction = response => ({
  type: ActionTypes.BOT_RESPONSE_RECEIVED,
  response
});
