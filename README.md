# TestingTask

Mongo DB schema:

```yaml
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "title": "Trainer",
  "description": "Schema for a trainer's training summary.",
  "properties": {
    "trainerUsername": {
      "type": "string",
      "description": "Unique username of the trainer."
    },
    "firstName": {
      "type": "string",
      "description": "First name of the trainer."
    },
    "lastName": {
      "type": "string",
      "description": "Last name of the trainer."
    },
    "status": {
      "type": "boolean",
      "description": "Trainer's current status."
    },
    "years": {
      "type": "array",
      "description": "List of years with monthly training summaries.",
      "items": {
        "type": "object",
        "properties": {
          "year": {
            "type": "integer",
            "minimum": 1900,
            "maximum": 2100,
            "description": "Year of the training summary."
          },
          "trainingsSummaryDurations": {
            "type": "array",
            "description": "Training summary durations for each month (January to December).",
            "items": {
              "type": "integer",
              "minimum": 0,
              "description": "Total training duration in hours for the month."
            },
            "minItems": 12,
            "maxItems": 12
          }
        },
        "required": ["year", "trainingsSummaryDurations"]
      }
    }
  },
  "required": ["trainerUsername", "firstName", "lastName", "status", "years"]
}
```