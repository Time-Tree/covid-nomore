import { combineReducers } from 'redux';
import { store } from './store';

import settingsReducer from './settings/reducer';
import eventsReducer from './events/reducer';

const rootReducer = combineReducers({
  settings: settingsReducer,
  events: eventsReducer
});

export default rootReducer;
