package middlewares

import (
	ratelimit "github.com/JGLTechnologies/gin-rate-limit"
	"github.com/gin-gonic/gin"
	"net/http"
	"time"
)

var RateLimitMiddleWare gin.HandlerFunc
var rateLimitStore ratelimit.Store

func init() {
	rateLimitStore = ratelimit.InMemoryStore(
		&ratelimit.InMemoryOptions{
			Rate:  time.Second * 10,
			Limit: 5,
			Skip:  nil,
		})
	RateLimitMiddleWare = ratelimit.RateLimiter(
		rateLimitStore, &ratelimit.Options{
			ErrorHandler:   rateLimitErrorHandler,
			KeyFunc:        filterByIPLimit,
			BeforeResponse: nil,
		},
	)
}

func filterByIPLimit(c *gin.Context) string {
	return c.ClientIP()
}

func rateLimitErrorHandler(c *gin.Context, info ratelimit.Info) {
	c.String(http.StatusTooManyRequests, "Too many requests. Try again in "+time.Until(info.ResetTime).String())
}
