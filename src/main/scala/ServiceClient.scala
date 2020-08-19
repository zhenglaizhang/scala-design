import java.util.UUID
import java.util.concurrent.Future

trait ServiceClient[T] {
  def create(t: T): Future[ServiceResponse[T]];

  def read(id: UUID): Future[ServiceResponse[T]];

  def update(id: UUID, t: T): Future[ServiceResponse[T]];

  def delete(id: UUID): Future[ServiceResponse[Boolean]]
}