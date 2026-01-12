package placeholder

import "dawpitech/area/models"

func HandlerEmpty(_ models.Context) error {
	return nil
}

var Provider = models.Service{
	Name:   "Placeholder",
	Hidden: true,
	Actions: []models.Action{
		{
			Name:          "none_action",
			PrettyName:    "None",
			Description:   "Empty action, will never activate",
			Parameters:    nil,
			SetupTrigger:  HandlerEmpty,
			RemoveTrigger: HandlerEmpty,
		},
	},
	Modifiers: []models.Modifier{
		{
			Name:        "none_modifier",
			PrettyName:  "None",
			Description: "Empty modifier",
			Parameters:  nil,
			Handler:     HandlerEmpty,
		},
	},
	Reactions: []models.Reaction{
		{
			Name:        "none_reaction",
			PrettyName:  "None",
			Description: "Empty reaction",
			Parameters:  nil,
			Handler:     HandlerEmpty,
		},
	},
	AuthMethod: nil,
	DBModels:   nil,
}
