package placeholder

import "dawpitech/area/models"

func HandlerEmpty(_ models.HandlerContext) error {
	return nil
}

func TriggerEmpty(_ models.TriggerContext) error {
	return nil
}

var Provider = models.Service{
	Name: "Placeholder",
	Icon: "",
	Actions: []models.Action{
		{
			Name:          "action_none",
			PrettyName:    "None",
			Description:   "Empty action, will never activate",
			Parameters:    []string{},
			SetupTrigger:  TriggerEmpty,
			RemoveTrigger: TriggerEmpty,
		},
	},
	Reactions: []models.Reaction{
		{
			Name:        "reaction_none",
			PrettyName:  "None",
			Description: "Empty reaction",
			Parameters:  []string{},
			Handler:     HandlerEmpty,
		},
	},
	AuthMethod: nil,
	DBModels:   []interface{}{},
}
