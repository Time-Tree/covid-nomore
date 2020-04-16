import { ActionTypes, initialState } from './store';

function settingsReducer(state = initialState, action) {
  switch (action.type) {
    case ActionTypes.CHANGE_PUBLISH_CODE: {
      return {
        ...state,
        publishCode: action.payload
      };
    }
    default:
      return state;
  }
}

export default settingsReducer;
