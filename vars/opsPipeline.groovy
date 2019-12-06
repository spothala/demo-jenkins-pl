import static com.example.demo.logger.Echoer.setStream
import com.example.demo.handler.OpsPipeline

def call(Closure body = { }) {
  setStream(this)
  OpsPipeline ops = new OpsPipeline(this)
  ops.trigger(body)
}
